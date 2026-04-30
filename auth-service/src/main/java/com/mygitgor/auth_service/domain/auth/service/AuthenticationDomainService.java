package com.mygitgor.auth_service.domain.auth.service;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.aggregate.AuthAggregate;
import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.TokenStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.model.port.JwtPort;
import com.mygitgor.auth_service.domain.auth.repository.BlacklistedTokenRepository;
import com.mygitgor.auth_service.domain.auth.repository.TokenRepository;
import com.mygitgor.auth_service.domain.auth.repository.VerificationCodeRepository;
import com.mygitgor.auth_service.domain.seller.port.SellerPort;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.domain.specification.OtpValiditySpecification;
import com.mygitgor.auth_service.domain.specification.TokenValiditySpecification;
import com.mygitgor.auth_service.domain.user.port.UserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationDomainService {

    private final TokenRepository tokenRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserPort userPort;
    private final SellerPort sellerPort;
    private final JwtPort jwtPort;
    private final OtpValiditySpecification otpValiditySpecification;
    private final TokenValiditySpecification tokenValiditySpecification;

    @Transactional
    public Mono<Token> authenticateWithOtp(Email email, String otpValue, OtpPurpose purpose) {
        log.info("Authenticating user with OTP: {}, purpose: {}", email, purpose);

        return Mono.fromCallable(() ->
                        verificationCodeRepository.findValidOtp(email, otpValue, purpose, LocalDateTime.now())
                                .orElseThrow(() -> new DomainException("Invalid or expired OTP"))
                )
                .flatMap(verificationCode -> {
                    try {
                        otpValiditySpecification.check(verificationCode);
                    } catch (DomainException e) {
                        return Mono.error(e);
                    }

                    verificationCode.markAsUsed();
                    verificationCodeRepository.save(verificationCode);

                    return getUserInfoAndGenerateToken(email);
                })
                .doOnSuccess(token -> log.info("User authenticated successfully: {}", email))
                .doOnError(error -> log.error("Authentication failed for {}: {}", email, error.getMessage()));
    }

    private Mono<Token> getUserInfoAndGenerateToken(Email email) {
        return userPort.existsByEmail(email)
                .flatMap(userExists -> {
                    if (userExists) {
                        return userPort.getUserByEmail(email)
                                .flatMap(user -> generateToken(email, user.getId(), UserRole.ROLE_CUSTOMER));
                    } else {
                        return sellerPort.existsByEmail(email)
                                .flatMap(sellerExists -> {
                                    if (sellerExists) {
                                        return sellerPort.getSellerByEmail(email)
                                                .flatMap(seller -> generateToken(email, seller.getUserId(), UserRole.ROLE_SELLER));
                                    } else {
                                        return Mono.error(new DomainException("User not found with email: " + email));
                                    }
                                });
                    }
                });
    }

    private Mono<Token> generateToken(Email email, UserId userId, UserRole role) {
        return jwtPort.generateToken(email.toString(), userId.toString(), role)
                .map(jwt -> Token.builder()
                        .value(new TokenValue(jwt))
                        .email(email)
                        .userId(userId)
                        .role(role)
                        .issuedAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusSeconds(jwtPort.getTokenExpirationSeconds()))
                        .status(TokenStatus.ACTIVE)
                        .build()
                )
                .flatMap(token -> {
                    return tokenRepository.findActiveTokenByUserId(userId)
                            .doOnNext(oldToken -> {
                                if (oldToken != null && oldToken.isValid()) {
                                    blacklistToken(oldToken, "New login from different device");
                                }
                            })
                            .then(Mono.just(token));
                })
                .flatMap(token -> tokenRepository.save(token).thenReturn(token));
    }

    @Transactional
    public Mono<Boolean> validateOtp(Email email, String otpValue, OtpPurpose purpose) {
        log.debug("Validating OTP for email: {}, purpose: {}", email, purpose);

        return Mono.fromCallable(() ->
                        verificationCodeRepository.findValidOtp(email, otpValue, purpose, LocalDateTime.now())
                                .orElseThrow(() -> new DomainException("Invalid or expired OTP"))
                )
                .map(verificationCode -> {
                    otpValiditySpecification.check(verificationCode);
                    verificationCode.markAsUsed();
                    verificationCodeRepository.save(verificationCode);
                    return true;
                })
                .onErrorReturn(false);
    }

    @Transactional
    public Mono<Token> refreshToken(Token oldToken) {
        log.info("Refreshing token for user: {}", oldToken.getEmail());

        return Mono.fromCallable(() -> {
                    tokenValiditySpecification.check(oldToken);
                    return oldToken;
                })
                .flatMap(token -> generateToken(
                        token.getEmail(),
                        token.getUserId(),
                        token.getRole()
                ))
                .flatMap(newToken -> {
                    blacklistToken(oldToken, "Token refreshed");
                    return tokenRepository.save(newToken).thenReturn(newToken);
                })
                .doOnSuccess(token -> log.info("Token refreshed successfully for user: {}", token.getEmail()))
                .doOnError(error -> log.error("Token refresh failed: {}", error.getMessage()));
    }

    @Transactional
    public Mono<Void> logout(Token token, String reason) {
        log.info("Logging out user: {}, reason: {}", token.getEmail(), reason);

        return Mono.fromRunnable(() -> blacklistToken(token, reason))
                .then(tokenRepository.delete(token))
                .doOnSuccess(v -> log.info("User logged out successfully: {}", token.getEmail()));
    }

    @Transactional
    public Mono<Void> logoutAllDevices(Email email) {
        log.info("Logging out user from all devices: {}", email);

        return tokenRepository.findAllByEmail(email)
                .collectList()
                .doOnNext(tokens -> {
                    for (Token token : tokens) {
                        if (token.isValid()) {
                            blacklistToken(token, "Logout from all devices");
                        }
                    }
                })
                .flatMap(tokens -> tokenRepository.deleteAllByEmail(email))
                .doOnSuccess(v -> log.info("User logged out from all devices: {}", email));
    }

    public Mono<Boolean> validateToken(Token token) {
        log.debug("Validating token for user: {}", token.getEmail());

        return Mono.fromCallable(() -> {
            try {
                tokenValiditySpecification.check(token);
                return true;
            } catch (DomainException e) {
                log.debug("Token validation failed: {}", e.getMessage());
                return false;
            }
        });
    }


    public Mono<Token> getTokenInfo(String tokenValue) {
        return tokenRepository.findByValue(new TokenValue(tokenValue))
                .flatMap(token -> {
                    if (token == null) {
                        return Mono.error(new DomainException("Token not found"));
                    }
                    return Mono.just(token);
                });
    }

    private void blacklistToken(Token token, String reason) {
        if (!token.isValid()) {
            log.debug("Token already invalid, skipping blacklist: {}", token.getEmail());
            return;
        }

        token.blacklist();
        tokenRepository.save(token);
        blacklistedTokenRepository.save(
                token.getValue().toString(),
                token.getUserId(),
                token.getExpiresAt()
        );

        log.debug("Token blacklisted for user: {}, reason: {}", token.getEmail(), reason);
    }

    public Mono<Boolean> isAccountActive(Email email) {
        return userPort.existsByEmail(email)
                .flatMap(userExists -> {
                    if (userExists) {
                        return userPort.getUserByEmail(email)
                                .map(user -> user.getAccountStatus() == AccountStatus.ACTIVE)
                                .defaultIfEmpty(false);
                    } else {
                        return sellerPort.existsByEmail(email)
                                .flatMap(sellerExists -> {
                                    if (sellerExists) {
                                        return sellerPort.getSellerByEmail(email)
                                                .map(seller -> seller.getAccountStatus() == AccountStatus.ACTIVE)
                                                .defaultIfEmpty(false);
                                    }
                                    return Mono.just(false);
                                });
                    }
                });
    }

    public Mono<Void> recordFailedLoginAttempt(Email email) {
        log.warn("Recording failed login attempt for user: {}", email);
        return Mono.empty();
    }
}
