package com.mygitgor.user_service.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.port.outgoing.KafkaEventPort;
import com.mygitgor.user_service.infrastructure.kafka.event.UserCreatedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.UserUpdatedEvent;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
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
        UserCreatedEvent event = new UserCreatedEvent(
                user.getId().getValue().toString(),
                user.getEmail().toString(),
                user.getFullName(),
                user.getRole().name(),
                user.getPhoneNumber(),
                user.getProfileImage(),
                LocalDateTime.now()
        );

        return sendEvent(USER_CREATED_TOPIC, event);
    }

    @Override
    public Mono<Void> sendUserUpdatedEvent(User user) {
        UserUpdatedEvent event = new UserUpdatedEvent(
                user.getId().getValue().toString(),
                user.getEmail().toString(),
                user.getFullName(),
                user.getRole().name(),
                LocalDateTime.now()
        );

        return sendEvent(USER_UPDATED_TOPIC, event);
    }

    @Override
    public Mono<Void> sendEmailVerifiedEvent(User user) {
        return null;
    }

    @Override
    public Mono<Void> sendUserDeletedEvent(Email user) {
        return null;
    }

    @Override
    public Mono<Void> sendPasswordChangedEvent(Email user) {
        return null;
    }

    @Override
    public Mono<Void> sendUserStatusChangedEvent(User user, String status, String reason, String changedBy) {
        return null;
    }

    @Override
    public Mono<Void> sendUserRoleChangedEvent(User user, String oldRole, String newRole, String changedBy) {
        return null;
    }

    @Override
    public Mono<Void> sendUserOrderStatsUpdatedEvent(User user) {
        return null;
    }

    private Mono<Void> sendEvent(String topic, Object event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(json -> Mono.fromFuture(kafkaTemplate.send(topic, json)))
                .doOnSuccess(result -> log.debug("Event sent to {}: {}", topic, event))
                .doOnError(error -> log.error("Failed to send event to {}", topic, error))
                .then();
    }
}
