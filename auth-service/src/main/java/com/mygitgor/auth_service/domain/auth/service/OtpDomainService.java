package com.mygitgor.auth_service.domain.auth.service;

import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.model.event.OtpGeneratedEvent;
import com.mygitgor.auth_service.domain.auth.repository.VerificationCodeRepository;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.Otp;
import com.mygitgor.auth_service.domain.specification.OtpValiditySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
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
                .email(email.toString())
                .otp(otpValue)
                .purpose(purpose)
                .expiresAt(otp.getExpiresAt())
                .occurredAt(LocalDateTime.now())
                .build());

        log.info("OTP generated for email: {}, purpose: {}", email, purpose);
        return savedCode;
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

