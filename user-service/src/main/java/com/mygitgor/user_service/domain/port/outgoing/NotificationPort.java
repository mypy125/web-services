package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.shared.valueobject.Email;
import reactor.core.publisher.Mono;

public interface NotificationPort {
    Mono<Void> sendWelcomeEmail(Email email, String name);
    Mono<Void> sendEmailVerifiedNotification(Email email);
    Mono<Void> sendPasswordChangedNotification(Email email);
    Mono<Void> sendAccountActivatedNotification(Email email);
    Mono<Void> sendAccountBannedNotification(Email email, String reason);
    Mono<Void> sendAccountSuspendedNotification(Email email, String reason);
}
