package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import reactor.core.publisher.Mono;

public interface NotificationPort {
    Mono<Void> sendWelcomeEmail(Email email, String name);
    Mono<Void> sendEmailVerifiedNotification(Email email);
    Mono<Void> sendPasswordChangedNotification(Email email);
}
