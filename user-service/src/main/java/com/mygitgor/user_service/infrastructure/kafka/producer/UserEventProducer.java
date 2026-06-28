package com.mygitgor.user_service.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.port.outgoing.KafkaEventPort;
import com.mygitgor.user_service.infrastructure.kafka.event.*;
import com.mygitgor.user_service.shared.valueobject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static com.mygitgor.user_service.infrastructure.kafka.KafkaTopics.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer implements KafkaEventPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> sendUserCreatedEvent(User user) {
        String userId = user.getId().getValue().toString();
        UserCreatedEvent event = new UserCreatedEvent(
                user.getId().getValue().toString(),
                user.getEmail().toString(),
                user.getFullName(),
                user.getRole().name(),
                user.getPhoneNumber(),
                user.getProfileImage(),
                LocalDateTime.now()
        );

        return sendEvent(USER_CREATED_TOPIC, userId, event);
    }

    @Override
    public Mono<Void> sendUserUpdatedEvent(User user) {
        String userId = user.getId().getValue().toString();
        UserUpdatedEvent event = new UserUpdatedEvent(
                user.getId().getValue().toString(),
                user.getEmail().toString(),
                user.getFullName(),
                user.getRole().name(),
                LocalDateTime.now()
        );

        return sendEvent(USER_UPDATED_TOPIC, userId, event);
    }

    @Override
    public Mono<Void> sendEmailVerifiedEvent(User user) {
        String userId = user.getId().getValue().toString();
        EmailVerifiedEvent event = new EmailVerifiedEvent(
                user.getId().getValue().toString(),
                user.getEmail().toString(),
                user.getEmailVerifiedAt(),
                LocalDateTime.now()
        );

        return sendEvent(EMAIL_VERIFIED_TOPIC, userId, event);
    }

    @Override
    public Mono<Void> sendUserDeletedEvent(Email email) {
        String key = email.toString();
        UserDeletedEvent event = new UserDeletedEvent(
                email.toString(),
                LocalDateTime.now()
        );

        return sendEvent(USER_DELETED_TOPIC, key, event);
    }

    @Override
    public Mono<Void> sendPasswordChangedEvent(Email email) {
        String key = email.toString();
        PasswordChangedEvent event = new PasswordChangedEvent(
                email.toString(),
                LocalDateTime.now()
        );

        return sendEvent(PASSWORD_CHANGED_TOPIC, key, event);
    }

    @Override
    public Mono<Void> sendUserStatusChangedEvent(User user, String oldStatus, String reason, String changedBy) {
        String userId = user.getId().getValue().toString();
        UserStatusChangedEvent event = new UserStatusChangedEvent(
                user.getId().getValue().toString(),
                user.getEmail().toString(),
                oldStatus != null ? oldStatus : user.getAccountStatus().name(),
                user.getAccountStatus().name(),
                reason,
                changedBy,
                LocalDateTime.now()
        );

        return sendEvent(USER_STATUS_CHANGED_TOPIC, userId, event);
    }

    @Override
    public Mono<Void> sendUserRoleChangedEvent(User user, String oldRole, String newRole, String changedBy) {
        String userId = user.getId().getValue().toString();
        UserRoleChangedEvent event = new UserRoleChangedEvent(
                user.getId().getValue().toString(),
                user.getEmail().toString(),
                oldRole,
                newRole != null ? newRole : user.getRole().name(),
                changedBy,
                LocalDateTime.now()
        );

        return sendEvent(USER_ROLE_CHANGED_TOPIC, userId, event);
    }

    @Override
    public Mono<Void> sendUserOrderStatsUpdatedEvent(User user) {
        String userId = user.getId().getValue().toString();
        UserOrderStatsUpdatedEvent event = new UserOrderStatsUpdatedEvent(
                user.getId().getValue().toString(),
                user.getEmail().toString(),
                user.getTotalOrdersCount() != null ? user.getTotalOrdersCount() : 0,
                user.getTotalSpentAmount() != null ? user.getTotalSpentAmount() : 0.0,
                LocalDateTime.now()
        );

        return sendEvent(USER_ORDER_STATS_UPDATED_TOPIC, userId, event);
    }

    private Mono<Void> sendEvent(String topic, String key, Object event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(json -> Mono.fromFuture(kafkaTemplate.send(topic, key, json)))
                .doOnSuccess(result -> log.debug("Event sent to {}: {}", topic, event))
                .doOnError(error -> log.error("Failed to send event to {}", topic, error))
                .then();
    }
}
