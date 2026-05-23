package com.mygitgor.auth_service.domain.auth.service;

import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.model.event.OtpGeneratedEvent;
import com.mygitgor.auth_service.domain.auth.model.port.NotificationPort;
import com.mygitgor.auth_service.domain.auth.repository.VerificationCodeRepository;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.Otp;
import com.mygitgor.auth_service.domain.specification.OtpValiditySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpDomainService {
    private static final int OTP_VALIDITY_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final VerificationCodeRepository verificationCodeRepository;
    private final OtpValiditySpecification otpValiditySpec;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationPort notificationPort;

    @Transactional
    public Mono<VerificationCode> generateOtp(Email email, UserRole role, OtpPurpose purpose) {
        log.info("Generating OTP for email: {}, purpose: {}", email, purpose);

        return invalidateExistingOtps(email, purpose)
                .then(Mono.fromCallable(() -> {
                    String otpValue = generateSecureOtp();
                    Otp otp = new Otp(otpValue, OTP_VALIDITY_MINUTES);

                    return VerificationCode.builder()
                            .id(UUID.randomUUID())
                            .otp(otp)
                            .email(email)
                            .userRole(role)
                            .purpose(purpose)
                            .createdAt(LocalDateTime.now())
                            .used(false)
                            .build();
                }))
                .flatMap(verificationCodeRepository::save)
                .doOnNext(savedCode -> {
                    eventPublisher.publishEvent(OtpGeneratedEvent.builder()
                            .source(this)
                            .email(email.toString())
                            .otp(savedCode.getOtp().getValue())
                            .purpose(purpose)
                            .expiresAt(savedCode.getOtp().getExpiresAt())
                            .occurredAt(LocalDateTime.now())
                            .build());

                    notificationPort.sendOtpEmail(email, savedCode.getOtp().getValue(), purpose)
                            .subscribe(
                                    success -> log.info("OTP email sent successfully to: {}", email),
                                    error -> log.error("Failed to send OTP email to: {}, error: {}", email, error.getMessage())
                            );
                })
                .doOnSuccess(savedCode -> log.info("OTP generated for email: {}, purpose: {}", email, purpose))
                .doOnError(error -> log.error("Failed to generate OTP for email: {}", email, error));
    }

    @Transactional
    public Mono<Void> generateAndSendOtp(Email email, UserRole role, OtpPurpose purpose) {
        log.info("Generating and sending OTP for email: {}, purpose: {}", email, purpose);

        return invalidateExistingOtps(email, purpose)
                .then(Mono.fromCallable(() -> {
                    String otpValue = generateSecureOtp();
                    Otp otp = new Otp(otpValue, OTP_VALIDITY_MINUTES);

                    return VerificationCode.builder()
                            .id(UUID.randomUUID())
                            .otp(otp)
                            .email(email)
                            .userRole(role)
                            .purpose(purpose)
                            .createdAt(LocalDateTime.now())
                            .used(false)
                            .build();
                }))
                .flatMap(verificationCodeRepository::save)
                .flatMap(savedCode -> {
                    eventPublisher.publishEvent(OtpGeneratedEvent.builder()
                            .source(this)
                            .email(email.toString())
                            .otp(savedCode.getOtp().getValue())
                            .purpose(purpose)
                            .expiresAt(savedCode.getOtp().getExpiresAt())
                            .occurredAt(LocalDateTime.now())
                            .build());

                    return notificationPort.sendOtpEmail(email, savedCode.getOtp().getValue(), purpose)
                            .thenReturn(savedCode);
                })
                .doOnSuccess(savedCode -> log.info("OTP generated and sent successfully to: {}", email))
                .doOnError(error -> log.error("Failed to generate/send OTP for {}: {}", email, error.getMessage()))
                .then();
    }

    public Mono<Boolean> validateOtp(Email email, String otpValue, OtpPurpose purpose) {
        log.debug("Validating OTP for email: {}, purpose: {}", email, purpose);

        return verificationCodeRepository.findByEmailAndOtpAndPurpose(email, otpValue, purpose)
                .flatMap(verificationCode -> {
                    try {
                        otpValiditySpec.check(verificationCode);
                        verificationCode.markAsUsed();
                        return verificationCodeRepository.save(verificationCode)
                                .thenReturn(true);
                    } catch (DomainException e) {
                        return Mono.error(e);
                    }
                })
                .doOnSuccess(valid -> {
                    if (Boolean.TRUE.equals(valid)) {
                        log.info("OTP validated for email: {}, purpose: {}", email, purpose);
                    }
                })
                .onErrorResume(e -> {
                    log.warn("OTP validation failed for email: {}, error: {}", email, e.getMessage());
                    return Mono.just(false);
                });
    }

    private String generateSecureOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private Mono<Void> invalidateExistingOtps(Email email, OtpPurpose purpose) {
        return verificationCodeRepository.findByEmailAndPurpose(email, purpose)
                .flatMap(code -> {
                    if (code.isValid()) {
                        code.markAsUsed();
                        return verificationCodeRepository.save(code);
                    }
                    return Mono.empty();
                })
                .then();
    }
}

