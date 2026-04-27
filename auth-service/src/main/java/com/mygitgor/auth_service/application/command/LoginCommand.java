package com.mygitgor.auth_service.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginCommand {
    String email;
    String otp;
    String ipAddress;
    String userAgent;
}