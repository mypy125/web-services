package com.mygitgor.auth_service.domain.auth.repository;

import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationCodeRepository {
    VerificationCode save(VerificationCode code);
    Optional<VerificationCode> findByEmailAndOtpAndPurpose(Email email, String otp, OtpPurpose purpose);
    Optional<VerificationCode> findValidOtp(Email email, String otp, OtpPurpose purpose, LocalDateTime now);
    List<VerificationCode> findByEmailAndPurpose(Email email, OtpPurpose purpose);
    List<VerificationCode> findByEmail(Email email);
    void invalidateAllOtpsForEmailAndPurpose(Email email, OtpPurpose purpose);
    void deleteExpiredCodes();
    long countRecentOtps(Email email, OtpPurpose purpose, LocalDateTime since);
    void delete(VerificationCode code);
    void deleteAllByEmail(Email email);
}
