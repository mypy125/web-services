package com.mygitgor.auth_service.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VerifyOtpCommand {
    String email;
    String otp;
    String purpose;
}
