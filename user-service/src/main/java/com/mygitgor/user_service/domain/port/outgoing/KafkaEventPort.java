package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import reactor.core.publisher.Mono;

public interface KafkaEventPort {
    Mono<Void> sendUserCreatedEvent(User user);
    Mono<Void> sendUserUpdatedEvent(User user);
    Mono<Void> sendEmailVerifiedEvent(User user);
    Mono<Void> sendUserDeletedEvent(Email user);
    Mono<Void> sendPasswordChangedEvent(Email user);
    Mono<Void> sendUserStatusChangedEvent(User user, String status, String reason, String changedBy);
}
