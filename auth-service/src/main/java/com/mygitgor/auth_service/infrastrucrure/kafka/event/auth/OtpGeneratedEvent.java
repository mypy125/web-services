package com.mygitgor.auth_service.infrastrucrure.kafka.event.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpGeneratedEvent {
    private String email;
    private String otp;
    private String purpose;
    private LocalDateTime expiresAt;
    private LocalDateTime occurredAt;
}
