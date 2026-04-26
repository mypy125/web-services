package com.mygitgor.auth_service.domain.model;

import com.mygitgor.auth_service.domain.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.model.enums.UserRole;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.Otp;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class VerificationCode {
    private final UUID id;
    private final Otp otp;
    private final Email email;
    private final UserRole userRole;
    private final OtpPurpose purpose;
    private final LocalDateTime createdAt;
    private boolean used;

    public void markAsUsed() {
        if (used) {
            throw new DomainException("OTP already used");
        }
        if (otp.isExpired()) {
            throw new DomainException("OTP has expired");
        }
        this.used = true;
    }

    public boolean isValid() {
        return !used && otp.isValid();
    }

    public void validate() {
        if (used) {
            throw new DomainException("OTP has already been used");
        }
        if (otp.isExpired()) {
            throw new DomainException("OTP has expired");
        }
    }
}
