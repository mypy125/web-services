package com.mygitgor.auth_service.application.dto.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationWebhookDto {
    private String email;
    private boolean verified;
    private LocalDateTime verifiedAt;
    private String verificationMethod;
    private String requestId;
}
