package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.domain.model.User;
import reactor.core.publisher.Mono;

public interface KafkaEventPort {
    Mono<Void> sendUserCreatedEvent(User user);
    Mono<Void> sendUserUpdatedEvent(User user);
}
