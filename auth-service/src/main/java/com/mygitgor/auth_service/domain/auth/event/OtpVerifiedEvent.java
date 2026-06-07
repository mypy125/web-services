package com.mygitgor.auth_service.domain.auth.event;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifiedEvent {
    private String eventId;
    private String otpCode;
    private String email;
    private String userId;
    private UserRole userRole;
    private OtpPurpose purpose;
    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private boolean success;
    private String failureReason;
    private int remainingAttempts;
    private LocalDateTime verifiedAt;
    private LocalDateTime occurredAt;
    private String verificationMethod;
}
