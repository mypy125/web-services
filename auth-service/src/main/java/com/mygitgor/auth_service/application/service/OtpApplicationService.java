package com.mygitgor.auth_service.application.service;

import com.mygitgor.auth_service.application.command.RequestOtpCommand;
import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.service.OtpDomainService;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpApplicationService {
    private final OtpDomainService otpDomainService;

    @Transactional
    public void requestOtp(RequestOtpCommand command) {
        Email email = new Email(command.getEmail());
        UserRole role = command.getRole();
        OtpPurpose purpose = command.getPurpose();

        VerificationCode code = otpDomainService.generateOtp(email, role, purpose);

        log.info("OTP requested for email: {}, purpose: {}", email, purpose);

    }

    public boolean verifyOtp(String email, String otp, String purposeStr) {
        Email emailVo = new Email(email);
        OtpPurpose purpose = OtpPurpose.valueOf(purposeStr);

        return otpDomainService.validateOtp(emailVo, otp, purpose);
    }
}
