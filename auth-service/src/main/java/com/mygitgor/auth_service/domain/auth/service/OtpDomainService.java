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
import java.util.List;
import java.util.Map;
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

    public VerificationCode generateOtp(Email email, UserRole role, OtpPurpose purpose) {
        invalidateExistingOtps(email, purpose);

        String otpValue = generateSecureOtp();
        Otp otp = new Otp(otpValue, OTP_VALIDITY_MINUTES);

        VerificationCode verificationCode = VerificationCode.builder()
                .id(UUID.randomUUID())
                .otp(otp)
                .email(email)
                .userRole(role)
                .purpose(purpose)
                .createdAt(LocalDateTime.now())
                .used(false)
                .build();

        VerificationCode savedCode = verificationCodeRepository.save(verificationCode);

        eventPublisher.publishEvent(OtpGeneratedEvent.builder()
                .source(this)
                .email(email.toString())
                .otp(otpValue)
                .purpose(purpose)
                .expiresAt(otp.getExpiresAt())
                .occurredAt(LocalDateTime.now())
                .build());

        notificationPort.sendOtpEmail(email, otpValue, purpose)
                .subscribe(
                        success -> log.info("OTP email sent successfully to: {}", email),
                        error -> log.error("Failed to send OTP email to: {}, error: {}", email, error.getMessage())
                );

        log.info("OTP generated for email: {}, purpose: {}", email, purpose);
        return savedCode;
    }

    @Transactional
    public Mono<Void> generateAndSendOtp(Email email, UserRole role, OtpPurpose purpose) {
        log.info("Generating and sending OTP for email: {}, purpose: {}", email, purpose);

        return Mono.fromCallable(() -> {
                    invalidateExistingOtps(email, purpose);
                    String otpValue = generateSecureOtp();
                    Otp otp = new Otp(otpValue, OTP_VALIDITY_MINUTES);

                    VerificationCode verificationCode = VerificationCode.builder()
                            .id(UUID.randomUUID())
                            .otp(otp)
                            .email(email)
                            .userRole(role)
                            .purpose(purpose)
                            .createdAt(LocalDateTime.now())
                            .used(false)
                            .build();

                    VerificationCode savedCode = verificationCodeRepository.save(verificationCode);

                    eventPublisher.publishEvent(OtpGeneratedEvent.builder()
                            .source(this)
                            .email(email.toString())
                            .otp(otpValue)
                            .purpose(purpose)
                            .expiresAt(otp.getExpiresAt())
                            .occurredAt(LocalDateTime.now())
                            .build());

                    return Map.of("code", savedCode, "otpValue", otpValue);
                })
                .flatMap(result -> notificationPort.sendOtpEmail(email, (String) result.get("otpValue"), purpose)
                        .thenReturn(result.get("code")))
                .doOnSuccess(code -> log.info("OTP generated and sent successfully to: {}", email))
                .doOnError(error -> log.error("Failed to generate/send OTP for {}: {}", email, error.getMessage()))
                .then();
    }

    public boolean validateOtp(Email email, String otpValue, OtpPurpose purpose) {
        VerificationCode verificationCode = verificationCodeRepository
                .findByEmailAndOtpValue(email, otpValue)
                .filter(code -> code.getPurpose() == purpose)
                .orElseThrow(() -> new DomainException("OTP not found"));

        otpValiditySpec.check(verificationCode);

        verificationCode.markAsUsed();
        verificationCodeRepository.save(verificationCode);

        log.info("OTP validated for email: {}, purpose: {}", email, purpose);
        return true;
    }

    private String generateSecureOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private void invalidateExistingOtps(Email email, OtpPurpose purpose) {
        List<VerificationCode> existingCodes = verificationCodeRepository
                .findByEmailAndPurpose(email, purpose);

        existingCodes.forEach(code -> {
            if (code.isValid()) {
                code.markAsUsed();
                verificationCodeRepository.save(code);
            }
        });
    }
}

