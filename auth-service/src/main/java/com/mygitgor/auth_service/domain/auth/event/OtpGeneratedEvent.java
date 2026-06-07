package com.mygitgor.auth_service.domain.auth.event;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpGeneratedEvent  {
    private String email;
    private String otp;
    private OtpPurpose purpose;
    private LocalDateTime expiresAt;
    private LocalDateTime occurredAt;
}
