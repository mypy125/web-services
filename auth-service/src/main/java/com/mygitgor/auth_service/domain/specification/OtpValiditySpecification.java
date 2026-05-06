package com.mygitgor.auth_service.domain.specification;

import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import org.springframework.stereotype.Component;

@Component
public class OtpValiditySpecification {

    public boolean isSatisfiedBy(VerificationCode code) {
        return code != null && code.isValid();
    }

    public void check(VerificationCode code) {
        if (code == null) {
            throw new DomainException("Verification code not found");
        }
        if (code.isUsed()) {
            throw new DomainException("OTP has already been used");
        }
        if (code.getOtp().isExpired()) {
            throw new DomainException("OTP has expired");
        }
    }
}
