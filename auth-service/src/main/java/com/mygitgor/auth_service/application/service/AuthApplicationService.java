package com.mygitgor.auth_service.application.service;

import com.mygitgor.auth_service.application.command.LoginCommand;
import com.mygitgor.auth_service.application.command.RegisterCustomerCommand;
import com.mygitgor.auth_service.application.command.RegisterSellerCommand;
import com.mygitgor.auth_service.application.dto.response.AuthResponseDto;
import com.mygitgor.auth_service.application.dto.response.UserInfoResponseDto;
import com.mygitgor.auth_service.application.mapper.CommandMapper;
import com.mygitgor.auth_service.application.mapper.ResponseMapper;
import com.mygitgor.auth_service.domain.auth.model.aggregate.AuthAggregate;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.service.AuthenticationDomainService;
import com.mygitgor.auth_service.domain.auth.service.OtpDomainService;
import com.mygitgor.auth_service.domain.auth.service.TokenDomainService;
import com.mygitgor.auth_service.domain.cart.port.CartPort;
import com.mygitgor.auth_service.domain.seller.model.Seller;
import com.mygitgor.auth_service.domain.seller.model.SellerVerificationStatus;
import com.mygitgor.auth_service.domain.seller.model.valueobject.Address;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BankDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BusinessDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.SellerId;
import com.mygitgor.auth_service.domain.seller.port.SellerPort;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.domain.user.model.User;
import com.mygitgor.auth_service.domain.user.port.UserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {
    private final AuthenticationDomainService authDomainService;
    private final OtpDomainService otpDomainService;
    private final TokenDomainService tokenDomainService;
    private final UserPort userPort;
    private final SellerPort sellerPort;
    private final CartPort cartPort;
    private final ResponseMapper responseMapper;

    @Transactional
    public Mono<AuthResponseDto> login(LoginCommand command) {
        log.info("Processing login for email: {}", command.getEmail());

        return Mono.fromCallable(() -> new Email(command.getEmail()))
                .flatMap(email ->
                        userPort.existsByEmail(email)
                                .flatMap(userExists -> {
                                    if (!userExists) {
                                        return sellerPort.existsByEmail(email)
                                                .flatMap(sellerExists -> {
                                                    if (!sellerExists) {
                                                        return Mono.error(new DomainException("User not found with email: " + email));
                                                    }
                                                    return Mono.just(UserRole.ROLE_SELLER);
                                                });
                                    }
                                    return Mono.just(UserRole.ROLE_CUSTOMER);
                                })
                                .flatMap(role ->
                                        authDomainService.authenticateWithOtp(email, command.getOtp(), OtpPurpose.LOGIN)
                                                .flatMap(token ->
                                                        updateLastLogin(email, role)
                                                                .thenReturn(token)
                                                )
                                )
                )
                .map(token -> responseMapper.toAuthResponseDto(token))
                .map(dto -> {
                    dto.setMessage("Login successful");
                    dto.setTimestamp(LocalDateTime.now());
                    return dto;
                })
                .doOnSuccess(response -> log.info("Login successful for email: {}", command.getEmail()))
                .doOnError(error -> log.error("Login failed for email: {}", command.getEmail(), error));
    }

    @Transactional
    public Mono<AuthResponseDto> registerCustomer(RegisterCustomerCommand command) {
        log.info("Processing customer registration for email: {}", command.getEmail());

        return Mono.fromCallable(() -> new Email(command.getEmail()))
                .flatMap(email -> {
                    UserId userId = new UserId();

                    return otpDomainService.validateOtp(email, command.getOtp(), OtpPurpose.REGISTRATION)
                            .flatMap(valid -> {
                                if (!valid) {
                                    return Mono.error(new DomainException("Invalid OTP"));
                                }

                                return userPort.existsByEmail(email)
                                        .flatMap(exists -> {
                                            if (exists) {
                                                return Mono.error(new DomainException("User already exists with email: " + email));
                                            }

                                            User newUser = User.register(email, command.getFullName(), UserRole.ROLE_CUSTOMER);

                                            return userPort.createUser(newUser)
                                                    .flatMap(createdUser -> cartPort.createCart(userId).thenReturn(createdUser))
                                                    .flatMap(createdUser -> {
                                                        AuthAggregate aggregate = authDomainService.registerNewUser(
                                                                email, userId, UserRole.ROLE_CUSTOMER
                                                        );

                                                        return tokenDomainService.generateToken(email, userId, UserRole.ROLE_CUSTOMER);
                                                    });
                                        });
                            });
                })
                .map(token -> responseMapper.toAuthResponseDto(token))
                .map(dto -> {
                    dto.setMessage("Registration successful");
                    dto.setTimestamp(LocalDateTime.now());
                    return dto;
                })
                .doOnSuccess(response -> log.info("Customer registered successfully: {}", command.getEmail()))
                .doOnError(error -> log.error("Customer registration failed for: {}", command.getEmail(), error));
    }

    @Transactional
    public Mono<AuthResponseDto> registerSeller(RegisterSellerCommand command) {
        log.info("Processing seller registration for email: {}", command.getEmail());

        return Mono.fromCallable(() -> new Email(command.getEmail()))
                .flatMap(email -> {
                    UserId userId = new UserId();

                    return sellerPort.existsByEmail(email)
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(new DomainException("Seller already exists with email: " + email));
                                }

                                BusinessDetails businessDetails = BusinessDetails.builder()
                                        .businessName(command.getBusinessDetails().getBusinessName())
                                        .businessEmail(command.getBusinessDetails().getBusinessEmail())
                                        .businessMobile(command.getBusinessDetails().getBusinessMobile())
                                        .businessAddress(command.getBusinessDetails().getBusinessAddress())
                                        .logo(command.getBusinessDetails().getLogo())
                                        .banner(command.getBusinessDetails().getBanner())
                                        .registrationNumber(command.getBusinessDetails().getRegistrationNumber())
                                        .taxId(command.getBusinessDetails().getTaxId())
                                        .website(command.getBusinessDetails().getWebsite())
                                        .description(command.getBusinessDetails().getDescription())
                                        .businessType(command.getBusinessDetails().getBusinessType())
                                        .build();

                                Seller newSeller = Seller.register(
                                        email, userId, command.getSellerName(), command.getMobile(), businessDetails
                                );

                                if (command.getBankDetails() != null) {
                                    BankDetails bankDetails = BankDetails.builder()
                                            .accountNumber(command.getBankDetails().getAccountNumber())
                                            .accountHolderName(command.getBankDetails().getAccountHolderName())
                                            .bankName(command.getBankDetails().getBankName())
                                            .bankCode(command.getBankDetails().getBankCode())
                                            .accountType(command.getBankDetails().getAccountType())
                                            .upiId(command.getBankDetails().getUpiId())
                                            .build();
                                    newSeller.updateBankDetails(bankDetails);
                                }

                                if (command.getPickupAddress() != null) {
                                    Address pickupAddress = Address.builder()
                                            .name(command.getPickupAddress().getName())
                                            .locality(command.getPickupAddress().getLocality())
                                            .address(command.getPickupAddress().getAddress())
                                            .city(command.getPickupAddress().getCity())
                                            .state(command.getPickupAddress().getState())
                                            .pinCode(command.getPickupAddress().getPinCode())
                                            .mobile(command.getPickupAddress().getMobile())
                                            .build();
                                    newSeller.updatePickupAddress(pickupAddress);
                                }

                                return sellerPort.createSeller(newSeller);
                            })
                            .flatMap(seller -> {
                                authDomainService.registerNewUser(email, userId, UserRole.ROLE_SELLER);

                                return otpDomainService.generateAndSendOtp(
                                                email, UserRole.ROLE_SELLER, OtpPurpose.EMAIL_VERIFICATION
                                        )
                                        .thenReturn(seller);
                            });
                })
                .map(seller -> AuthResponseDto.builder()
                        .email(seller.getEmail().toString())
                        .userId(seller.getUserId().toString())
                        .role(UserRole.ROLE_SELLER)
                        .message("Seller registration initiated. Please verify your email with the OTP sent.")
                        .requiresEmailVerification(true)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnSuccess(response -> log.info("Seller registration initiated: {}", command.getEmail()))
                .doOnError(error -> log.error("Seller registration failed for: {}", command.getEmail(), error));
    }

    @Transactional
    public Mono<AuthResponseDto> verifySellerAndLogin(String email, String otp) {
        log.info("Verifying seller email: {}", email);

        return Mono.fromCallable(() -> new Email(email))
                .flatMap(emailVo ->
                        otpDomainService.validateOtp(emailVo, otp, OtpPurpose.EMAIL_VERIFICATION)
                                .flatMap(valid -> {
                                    if (!valid) {
                                        return Mono.error(new DomainException("Invalid or expired OTP"));
                                    }

                                    return sellerPort.verifySellerEmail(emailVo);
                                })
                                .flatMap(seller -> {
                                    if (seller.getVerificationStatus() == SellerVerificationStatus.BUSINESS_VERIFIED) {
                                        return sellerPort.activateSeller(seller.getId())
                                                .then(sellerPort.getSellerByEmail(emailVo));
                                    }
                                    return Mono.just(seller);
                                })
                                .flatMap(seller ->
                                        tokenDomainService.generateToken(emailVo, seller.getUserId(), UserRole.ROLE_SELLER)
                                )
                )
                .map(token -> responseMapper.toAuthResponseDto(token))
                .map(dto -> {
                    dto.setMessage("Email verified and login successful");
                    dto.setTimestamp(LocalDateTime.now());
                    return dto;
                })
                .doOnSuccess(response -> log.info("Seller email verified and logged in: {}", email))
                .doOnError(error -> log.error("Seller verification failed for: {}", email, error));
    }

    @Transactional
    public Mono<AuthResponseDto> completeSellerVerification(String sellerId, boolean approve, String verifiedBy, String notes) {
        log.info("Completing seller verification for ID: {} - Approved: {}", sellerId, approve);

        return Mono.fromCallable(() -> new SellerId(sellerId))
                .flatMap(id -> sellerPort.getSellerById(id))
                .flatMap(seller -> {
                    if (approve) {
                        return sellerPort.verifySellerDocuments(seller.getId(), true, verifiedBy, notes)
                                .flatMap(verified -> sellerPort.activateSeller(seller.getId()))
                                .flatMap(activated ->
                                        tokenDomainService.generateToken(
                                                seller.getEmail(), seller.getUserId(), UserRole.ROLE_SELLER
                                        )
                                )
                                .map(token -> responseMapper.toAuthResponseDto(token))
                                .map(dto -> {
                                    dto.setMessage("Seller verification completed and account activated");
                                    dto.setTimestamp(LocalDateTime.now());
                                    return dto;
                                });
                    } else {
                        return sellerPort.updateAccountStatus(seller.getEmail(), "REJECTED")
                                .then(Mono.just(AuthResponseDto.builder()
                                        .email(seller.getEmail().toString())
                                        .userId(seller.getUserId().toString())
                                        .role(UserRole.ROLE_SELLER)
                                        .message("Seller verification rejected: " + notes)
                                        .timestamp(LocalDateTime.now())
                                        .build()));
                    }
                })
                .doOnSuccess(response -> log.info("Seller verification completed for ID: {} - Approved: {}", sellerId, approve))
                .doOnError(error -> log.error("Seller verification failed for ID: {}", sellerId, error));
    }

    @Transactional
    public Mono<Void> resendVerificationOtp(String email, UserRole role, OtpPurpose purpose) {
        log.info("Resending verification OTP for email: {}, role: {}, purpose: {}", email, role, purpose);

        return Mono.fromCallable(() -> new Email(email))
                .flatMap(emailVo -> otpDomainService.generateAndSendOtp(emailVo, role, purpose))
                .then()
                .doOnSuccess(v -> log.info("Verification OTP resent to: {}", email))
                .doOnError(error -> log.error("Failed to resend verification OTP to: {}", email, error));
    }

    @Transactional
    public Mono<Void> logout(String tokenValue, String email) {
        log.info("Processing logout for email: {}", email);

        return Mono.fromCallable(() -> new Email(email))
                .flatMap(emailVo ->
                        tokenDomainService.getTokenInfo(new TokenValue(tokenValue))
                                .flatMap(token -> authDomainService.logout(emailVo, token))
                )
                .doOnSuccess(v -> log.info("Logout successful for email: {}", email))
                .doOnError(error -> log.error("Logout failed for email: {}", email, error));
    }

    @Transactional
    public Mono<Void> logoutAllDevices(String email) {
        log.info("Processing logout from all devices for email: {}", email);

        return Mono.fromCallable(() -> new Email(email))
                .flatMap(emailVo -> tokenDomainService.logoutAllDevices(emailVo.toString()))
                .doOnSuccess(v -> log.info("Logged out from all devices for: {}", email))
                .doOnError(error -> log.error("Logout from all devices failed for: {}", email, error));
    }

    public Mono<Boolean> validateToken(String tokenValue) {
        log.debug("Validating token");

        return Mono.fromCallable(() -> new TokenValue(tokenValue))
                .flatMap(token -> tokenDomainService.validateToken(token))
                .doOnSuccess(valid -> log.debug("Token validation result: {}", valid))
                .doOnError(error -> log.error("Token validation failed: {}", error.getMessage()));
    }

    @Transactional
    public Mono<AuthResponseDto> refreshToken(String oldTokenValue) {
        log.info("Refreshing token");

        return Mono.fromCallable(() -> new TokenValue(oldTokenValue))
                .flatMap(tokenValue -> tokenDomainService.getTokenInfo(tokenValue))
                .flatMap(oldToken -> {
                    if (oldToken.isExpired()) {
                        return Mono.error(new DomainException("Token has expired"));
                    }

                    return tokenDomainService.generateToken(
                            oldToken.getEmail(), oldToken.getUserId(), oldToken.getRole()
                    );
                })
                .flatMap(newToken ->
                        tokenDomainService.blacklistToken(
                                        new TokenValue(oldTokenValue),
                                        newToken.getUserId().toString(),
                                        newToken.getExpiresAt()
                                )
                                .thenReturn(newToken)
                )
                .map(token -> responseMapper.toAuthResponseDto(token))
                .map(dto -> {
                    dto.setMessage("Token refreshed successfully");
                    dto.setTimestamp(LocalDateTime.now());
                    return dto;
                })
                .doOnSuccess(response -> log.info("Token refreshed successfully"))
                .doOnError(error -> log.error("Token refresh failed: {}", error.getMessage()));
    }

    public Mono<UserInfoResponseDto> getUserInfoFromToken(String tokenValue) {
        log.debug("Getting user info from token");

        return Mono.fromCallable(() -> new TokenValue(tokenValue))
                .flatMap(token -> tokenDomainService.getTokenInfo(token))
                .map(token -> responseMapper.toUserInfoResponseDto(
                        token.getEmail(), token.getUserId(), null
                ));
    }


    private Mono<Void> updateLastLogin(Email email, UserRole role) {
        if (role == UserRole.ROLE_CUSTOMER) {
            return userPort.updateLastLogin(email, LocalDateTime.now());
        } else if (role == UserRole.ROLE_SELLER) {
            return sellerPort.updateLastLogin(email, LocalDateTime.now());
        }
        return Mono.empty();
    }
}
