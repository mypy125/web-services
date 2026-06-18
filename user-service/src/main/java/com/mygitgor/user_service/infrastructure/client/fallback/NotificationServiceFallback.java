package com.mygitgor.user_service.infrastructure.client.fallback;

import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class NotificationServiceFallback {

    public Mono<Void> sendWelcomeEmail(Email email, String name) {
        log.warn("Fallback: Could not send welcome email to: {}", email);
        return Mono.empty();
    }

    public Mono<Void> sendEmailVerifiedNotification(Email email) {
        log.warn("Fallback: Could not send email verified notification to: {}", email);
        return Mono.empty();
    }

    public Mono<Void> sendPasswordChangedNotification(Email email) {
        log.warn("Fallback: Could not send password changed notification to: {}", email);
        return Mono.empty();
    }

    public Mono<Void> sendAccountActivatedNotification(Email email) {
        log.warn("Fallback: Could not send account activated notification to: {}", email);
        return Mono.empty();
    }

    public Mono<Void> sendAccountBannedNotification(Email email, String reason) {
        log.warn("Fallback: Could not send account banned notification to: {}, reason: {}", email, reason);
        return Mono.empty();
    }

    public Mono<Void> sendAccountSuspendedNotification(Email email, String reason) {
        log.warn("Fallback: Could not send account suspended notification to: {}, reason: {}", email, reason);
        return Mono.empty();
    }
}
