package com.mygitgor.auth_service.infrastrucrure.client.fallback;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class NotificationServiceFallback {

    public Mono<Void> sendOtpEmail(Email email, String otp, OtpPurpose purpose) {
        log.warn("Fallback: Cannot send OTP email to {}, OTP would be: {}", email, otp);
        return Mono.empty();
    }

    public Mono<Void> sendWelcomeEmail(Email email, String name) {
        log.warn("Fallback: Cannot send welcome email to {}", email);
        return Mono.empty();
    }

    public Mono<Void> sendEmailVerifiedNotification(Email email) {
        log.warn("Fallback: Cannot send email verified notification to {}", email);
        return Mono.empty();
    }

    public Mono<Void> sendPasswordChangedNotification(Email email) {
        log.warn("Fallback: Cannot send password changed notification to {}", email);
        return Mono.empty();
    }
}
