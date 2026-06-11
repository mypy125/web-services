package com.mygitgor.user_service.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.port.outgoing.KafkaEventPort;
import com.mygitgor.user_service.infrastructure.kafka.event.UserCreatedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer implements KafkaEventPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_CREATED_TOPIC = "user.created";
    private static final String USER_UPDATED_TOPIC = "user.updated";
    private static final String USER_DELETED_TOPIC = "user.deleted";
    private static final String EMAIL_VERIFIED_TOPIC = "user.email.verified";

    @Override
    public Mono<Void> sendUserCreatedEvent(User user) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId().toString())
                .email(user.getEmail().toString())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(USER_CREATED_TOPIC, event);
    }

    @Override
    public Mono<Void> sendUserUpdatedEvent(User user) {
        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .userId(user.getId().toString())
                .email(user.getEmail().toString())
                .fullName(user.getFullName())
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(USER_UPDATED_TOPIC, event);
    }

    private Mono<Void> sendEvent(String topic, Object event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(json -> Mono.fromFuture(kafkaTemplate.send(topic, json)))
                .doOnSuccess(result -> log.debug("Event sent to {}: {}", topic, event))
                .doOnError(error -> log.error("Failed to send event to {}", topic, error))
                .then();
    }
}
