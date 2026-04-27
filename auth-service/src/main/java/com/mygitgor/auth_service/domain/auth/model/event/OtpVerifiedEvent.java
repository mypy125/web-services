package com.mygitgor.auth_service.domain.auth.model.event;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class OtpVerifiedEvent extends ApplicationEvent {
    private final String eventId;
    private final String otpCode;
    private final String email;
    private final String userId;
    private final UserRole userRole;
    private final OtpPurpose purpose;
    private final String deviceId;
    private final String ipAddress;
    private final String userAgent;
    private final boolean success;
    private final String failureReason;
    private final int remainingAttempts;
    private final LocalDateTime verifiedAt;
    private final LocalDateTime occurredAt;
    private final String verificationMethod;

    @Builder
    public OtpVerifiedEvent(Object source,
                            String otpCode,
                            String email,
                            String userId,
                            UserRole userRole,
                            OtpPurpose purpose,
                            String deviceId,
                            String ipAddress,
                            String userAgent,
                            boolean success,
                            String failureReason,
                            int remainingAttempts,
                            LocalDateTime verifiedAt,
                            LocalDateTime occurredAt,
                            String verificationMethod) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.otpCode = maskOtpCode(otpCode);
        this.email = email;
        this.userId = userId;
        this.userRole = userRole;
        this.purpose = purpose;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.success = success;
        this.failureReason = failureReason;
        this.remainingAttempts = remainingAttempts;
        this.verifiedAt = verifiedAt != null ? verifiedAt : LocalDateTime.now();
        this.occurredAt = occurredAt != null ? occurredAt : LocalDateTime.now();
        this.verificationMethod = verificationMethod != null ? verificationMethod : "OTP";
    }

    public static OtpVerifiedEvent success(Object source,
                                           String otpCode,
                                           String email,
                                           String userId,
                                           UserRole userRole,
                                           OtpPurpose purpose,
                                           String deviceId,
                                           String ipAddress,
                                           String userAgent,
                                           LocalDateTime verifiedAt) {
        return OtpVerifiedEvent.builder()
                .source(source)
                .otpCode(otpCode)
                .email(email)
                .userId(userId)
                .userRole(userRole)
                .purpose(purpose)
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .verifiedAt(verifiedAt)
                .remainingAttempts(0)
                .verificationMethod("OTP")
                .build();
    }

    public static OtpVerifiedEvent failure(Object source,
                                           String otpCode,
                                           String email,
                                           String userId,
                                           UserRole userRole,
                                           OtpPurpose purpose,
                                           String deviceId,
                                           String ipAddress,
                                           String userAgent,
                                           String failureReason,
                                           int remainingAttempts) {
        return OtpVerifiedEvent.builder()
                .source(source)
                .otpCode(otpCode)
                .email(email)
                .userId(userId)
                .userRole(userRole)
                .purpose(purpose)
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(false)
                .failureReason(failureReason)
                .remainingAttempts(remainingAttempts)
                .verifiedAt(LocalDateTime.now())
                .verificationMethod("OTP")
                .build();
    }

    public static OtpVerifiedEvent resend(Object source,
                                          String email,
                                          String userId,
                                          UserRole userRole,
                                          OtpPurpose purpose,
                                          String deviceId,
                                          String ipAddress) {
        return OtpVerifiedEvent.builder()
                .source(source)
                .otpCode(null)
                .email(email)
                .userId(userId)
                .userRole(userRole)
                .purpose(purpose)
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .success(true)
                .verifiedAt(LocalDateTime.now())
                .remainingAttempts(0)
                .verificationMethod("RESEND")
                .build();
    }

    private static String maskOtpCode(String otpCode) {
        if (otpCode == null || otpCode.length() < 6) {
            return "***";
        }
        return otpCode.substring(0, 2) + "**" + otpCode.substring(otpCode.length() - 2);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isEmailVerification() {
        return purpose == OtpPurpose.EMAIL_VERIFICATION;
    }

    public boolean isLoginVerification() {
        return purpose == OtpPurpose.LOGIN;
    }

    public boolean isRegistrationVerification() {
        return purpose == OtpPurpose.REGISTRATION;
    }

    public boolean isPasswordResetVerification() {
        return purpose == OtpPurpose.PASSWORD_RESET;
    }

    @Override
    public String toString() {
        return String.format("OtpVerifiedEvent{email=%s, purpose=%s, success=%s, verifiedAt=%s}",
                email, purpose, success, verifiedAt);
    }
}
