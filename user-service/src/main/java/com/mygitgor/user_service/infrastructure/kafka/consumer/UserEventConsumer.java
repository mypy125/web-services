package com.mygitgor.user_service.infrastructure.kafka.consumer;

import com.mygitgor.user_service.infrastructure.cache.UserCacheService;
import com.mygitgor.user_service.infrastructure.kafka.event.EmailVerifiedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.UserCreatedEvent;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {
    private final UserCacheService cacheService;

    @KafkaListener(topics = "user.created", groupId = "user-service-internal-group")
    public Mono<Void> handleUserCreated(UserCreatedEvent event) {
        log.info("Handling internal UserCreatedEvent for user ID: {}, email: {}",
                event.userId(), event.email());

        return Mono.empty();
    }

    @KafkaListener(topics = "user.verified", groupId = "user-service-internal-group")
    public Mono<Void> handleEmailVerified(EmailVerifiedEvent event) {
        log.info("Handling internal EmailVerifiedEvent for user ID: {}, verified at: {}",
                event.userId(), event.verifiedAt());

        if (event.userId() == null || event.userId().isBlank()) {
            log.warn("Received EmailVerifiedEvent with empty userId, skipping.");
            return Mono.empty();
        }

        try {
            UserId userId = new UserId(UUID.fromString(event.userId()));

            return cacheService.evictUserDashboardCache(userId)
                    .doOnSuccess(v -> log.debug("Successfully evicted user cache after email verification for ID: {}", userId))
                    .onErrorResume(e -> {
                        log.error("Failed to evict cache for verified user ID: {}", userId, e);
                        return Mono.empty();
                    });

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in EmailVerifiedEvent: '{}'", event.userId(), e);
            return Mono.empty();
        }
    }
}
