package com.mygitgor.auth_service.application.command;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RequestOtpCommand {
    String email;
    String otp;
    UserRole role;
    OtpPurpose purpose;
}
