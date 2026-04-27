package com.mygitgor.auth_service.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterCustomerCommand {
    String email;
    String fullName;
    String otp;
    String ipAddress;
}
