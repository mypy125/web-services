package com.mygitgor.auth_service.domain.auth.service;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.TokenStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.port.JwtPort;
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
import com.mygitgor.auth_service.domain.user.event.UserLoggedInEvent;
import com.mygitgor.auth_service.domain.user.event.UserRegisteredEvent;
import com.mygitgor.auth_service.domain.user.model.User;
import com.mygitgor.auth_service.domain.user.port.UserPort;
import com.mygitgor.auth_service.infrastrucrure.cache.TokenCacheService;
import com.mygitgor.auth_service.infrastrucrure.config.KafkaConfig;
import com.mygitgor.auth_service.infrastrucrure.kafka.event.auth.UserLoggedOutEvent;
import com.mygitgor.auth_service.infrastrucrure.kafka.producer.KafkaEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationDomainService {
    private final TokenRepository tokenRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final OtpValiditySpecification otpValiditySpecification;
    private final TokenValiditySpecification tokenValiditySpecification;
    private final KafkaEventProducer kafkaEventProducer;
    private final TokenCacheService tokenCacheService;
    private final UserPort userPort;
    private final SellerPort sellerPort;
    private final JwtPort jwtPort;

    @Transactional
    public Mono<Token> authenticateWithOtp(Email email, String otpValue, OtpPurpose purpose) {
        log.info("Authenticating user with OTP: {}, purpose: {}", email, purpose);

        return verificationCodeRepository.findValidOtp(email, otpValue, purpose, LocalDateTime.now())
                .switchIfEmpty(Mono.error(new DomainException("Invalid or expired OTP")))
                .flatMap(verificationCode -> {
                    try {
                        otpValiditySpecification.check(verificationCode);
                    } catch (DomainException e) {
                        return Mono.error(e);
                    }

                    verificationCode.markAsUsed();
                    return verificationCodeRepository.save(verificationCode)
                            .then(getUserInfoAndGenerateToken(email));
                })
                .flatMap(token -> {
                    sendKafkaUserLoggedInEvent(email, token).subscribe();

                    Map<String, Object> tokenInfo = createTokenInfo(token);
                    long ttlSeconds = jwtPort.getTokenExpirationSeconds();

                    return tokenCacheService.cacheActiveToken(email.toString(), tokenInfo, ttlSeconds)
                            .then(updateLastLogin(email, token.getRole()))
                            .thenReturn(token);
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
                                                .flatMap(seller -> Mono.justOrEmpty(seller.getUserId())
                                                        .switchIfEmpty(Mono.error(new DomainException(
                                                                "Seller account exists but user association missing for email: " + email)))
                                                        .flatMap(userId -> generateToken(email, userId, UserRole.ROLE_SELLER)));
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
                .flatMap(token -> tokenRepository.findActiveTokenByUserId(userId)
                        .doOnNext(oldToken -> {
                            if (oldToken != null && oldToken.isValid()) {
                                blacklistToken(oldToken, "New login from different device");
                            }
                        })
                        .then(Mono.just(token)))
                .flatMap(token -> tokenRepository.save(token).thenReturn(token));
    }

    @Transactional
    public Mono<Void> registerNewUser(Email email, UserId userId, UserRole role) {
        log.info("Registering new user: {} with role: {}", email, role);

        User newUser = User.builder()
                .id(userId)
                .email(email)
                .role(role)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userPort.createUser(newUser)
                .doOnSuccess(user -> {
                    log.info("User registered successfully: {}", email);
                    sendKafkaUserRegisteredEvent(email, userId, role, null, null, null).subscribe();
                })
                .doOnError(error -> log.error("Failed to register user: {}, error: {}", email, error.getMessage()))
                .then();
    }

    @Transactional
    public Mono<Boolean> validateOtp(Email email, String otpValue, OtpPurpose purpose) {
        log.debug("Validating OTP for email: {}, purpose: {}", email, purpose);

        return verificationCodeRepository.findValidOtp(email, otpValue, purpose, LocalDateTime.now())
                .switchIfEmpty(Mono.error(new DomainException("Invalid or expired OTP")))
                .flatMap(verificationCode -> {
                    try {
                        otpValiditySpecification.check(verificationCode);
                        verificationCode.markAsUsed();
                        return verificationCodeRepository.save(verificationCode)
                            .thenReturn(true);
                    }catch (DomainException e) {
                        return Mono.error(e);
                    }

                })
                .onErrorResume(e -> {
                    log.warn("OTP validation failed: {}", e.getMessage());
                    return Mono.just(false);
                });
    }

    @Transactional
    public Mono<Token> refreshToken(Token oldToken) {
        log.info("Refreshing token for user: {}", oldToken.getEmail());

        return tokenValiditySpecification.check(oldToken)
                .then(Mono.defer(() -> generateToken(
                        oldToken.getEmail(),
                        oldToken.getUserId(),
                        oldToken.getRole()
                )))
                .flatMap(newToken -> {
                    return tokenCacheService.blacklistToken(oldToken.getValue().toString(), oldToken.getExpiresAt())
                            .then(tokenCacheService.removeActiveToken(oldToken.getEmail().toString()))
                            .then(blacklistTokenInDb(oldToken, "Token refreshed"))
                            .then(tokenRepository.save(newToken))
                            .thenReturn(newToken);
                })
                .flatMap(newToken -> {
                    Map<String, Object> tokenInfo = createTokenInfo(newToken);
                    long ttlSeconds = jwtPort.getTokenExpirationSeconds();

                    return tokenCacheService.cacheActiveToken(newToken.getEmail().toString(), tokenInfo, ttlSeconds)
                            .thenReturn(newToken);
                })
                .doOnSuccess(token -> log.info("Token refreshed successfully for user: {}", token.getEmail()))
                .doOnError(error -> log.error("Token refresh failed: {}", error.getMessage()));
    }

    @Transactional
    public Mono<Void> logout(Token token, String reason) {
        log.info("Logging out user: {}, reason: {}", token.getEmail(), reason);

        return tokenCacheService.blacklistToken(token.getValue().toString(), token.getExpiresAt())
                .then(tokenCacheService.removeActiveToken(token.getEmail().toString()))
                .then(blacklistTokenInDb(token, reason))
                .then(tokenRepository.delete(token))
                .doOnSuccess(v -> {
                    log.info("User logged out successfully: {}", token.getEmail());
                    sendKafkaUserLoggedOutEvent(token, reason).subscribe();
                });
    }

    @Transactional
    public Mono<Void> logoutByEmail(Email email, String reason) {
        log.info("Logging out all devices for user: {}, reason: {}", email, reason);

        return tokenRepository.findAllByEmail(email)
                .flatMap(token -> {
                    if (token.isValid()) {
                        return tokenCacheService.blacklistToken(token.getValue().toString(), token.getExpiresAt())
                                .then(blacklistTokenInDb(token, reason))
                                .then(tokenRepository.delete(token));
                    }
                    return Mono.empty();
                })
                .then(tokenCacheService.removeActiveToken(email.toString()).then())
                .doOnSuccess(v -> log.info("All tokens blacklisted for user: {}", email));
    }

    @Transactional
    public Mono<Void> logoutAllDevices(Email email) {
        log.info("Logging out user from all devices: {}", email);

        return tokenRepository.findAllByEmail(email)
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .flatMap(token -> {
                    if (token.isValid()) {
                        return tokenCacheService.blacklistToken(token.getValue().toString(), token.getExpiresAt())
                                .then(blacklistTokenInDb(token, "Logout from all devices"))
                                .then(tokenRepository.delete(token));
                    }
                    return Mono.empty();
                })
                .then(tokenCacheService.removeActiveToken(email.toString()))
                .then(tokenRepository.deleteAllByEmail(email))
                .doOnSuccess(v -> log.info("User logged out from all devices: {}", email));
    }

    private Mono<Void> updateLastLogin(Email email, UserRole role) {
        if (role == UserRole.ROLE_CUSTOMER) {
            return userPort.updateLastLogin(email, LocalDateTime.now())
                    .doOnSuccess(v -> log.debug("Last login updated for customer: {}", email))
                    .doOnError(error -> log.error("Failed to update last login for customer: {}", email, error));
        } else if (role == UserRole.ROLE_SELLER) {
            return sellerPort.updateLastLogin(email, LocalDateTime.now())
                    .doOnSuccess(v -> log.debug("Last login updated for seller: {}", email))
                    .doOnError(error -> log.error("Failed to update last login for seller: {}", email, error));
        }
        return Mono.empty();
    }

    public Mono<Boolean> validateToken(Token token) {
        log.debug("Validating token for user: {}", token.getEmail());

        return tokenCacheService.isTokenBlacklisted(token.getValue().toString())
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.debug("Token is blacklisted in Redis: {}", token.getValue());
                        return Mono.just(false);
                    }

                    try {
                        tokenValiditySpecification.check(token);
                        return Mono.just(true);
                    } catch (DomainException e) {
                        log.debug("Token validation failed: {}", e.getMessage());
                        return Mono.just(false);
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

    private Mono<Void> blacklistTokenInDb(Token token, String reason) {
        if (!token.isValid()) {
            log.debug("Token already invalid, skipping blacklist: {}", token.getEmail());
            return Mono.empty();
        }

        token.blacklist();

        return tokenRepository.save(token)
                .flatMap(savedToken ->
                        blacklistedTokenRepository.save(
                                token.getValue().toString(),
                                token.getUserId(),
                                token.getExpiresAt()
                        )
                )
                .doOnSuccess(v -> log.debug("Token blacklisted in DB for user: {}, reason: {}", token.getEmail(), reason));
    }

    private Map<String, Object> createTokenInfo(Token token) {
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("token", token.getValue().toString());
        tokenInfo.put("userId", token.getUserId().toString());
        tokenInfo.put("role", token.getRole().name());
        return tokenInfo;
    }

    private Mono<Void> sendKafkaUserLoggedInEvent(Email email, Token token) {
        UserLoggedInEvent kafkaEvent = UserLoggedInEvent.builder()
                        .email(email.toString())
                        .userId(token.getUserId().toString())
                        .token(token.getValue().toString())
                        .role(token.getRole().name())
                        .occurredAt(LocalDateTime.now())
                        .build();

        return kafkaEventProducer.sendEvent(KafkaConfig.USER_LOGGED_IN_TOPIC, kafkaEvent);
    }

    private Mono<Void> sendKafkaUserRegisteredEvent(Email email, UserId userId, UserRole role, String name, String deviceId, String ipAddress) {
        UserRegisteredEvent kafkaEvent = UserRegisteredEvent.builder()
                        .email(email.toString())
                        .userId(userId.toString())
                        .name(name)
                        .role(role)
                        .deviceId(deviceId)
                        .ipAddress(ipAddress)
                        .occurredAt(LocalDateTime.now())
                        .build();

        return kafkaEventProducer.sendEvent(KafkaConfig.USER_REGISTERED_TOPIC, kafkaEvent);
    }

    private Mono<Void> sendKafkaUserLoggedOutEvent(Token token, String reason) {
        UserLoggedOutEvent kafkaEvent = UserLoggedOutEvent.builder()
                        .email(token.getEmail().toString())
                        .userId(token.getUserId().toString())
                        .reason(reason)
                        .occurredAt(LocalDateTime.now())
                        .build();

        return kafkaEventProducer.sendEvent(KafkaConfig.USER_LOGGED_OUT_TOPIC, kafkaEvent);
    }
}
