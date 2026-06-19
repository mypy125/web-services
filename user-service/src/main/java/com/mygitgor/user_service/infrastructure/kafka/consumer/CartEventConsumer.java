package com.mygitgor.user_service.infrastructure.kafka.consumer;

import com.mygitgor.user_service.infrastructure.cache.UserCacheService;
import com.mygitgor.user_service.infrastructure.kafka.event.CartAbandonedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.CartUpdatedEvent;
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
public class CartEventConsumer {
    private final UserCacheService cacheService;

    @KafkaListener(topics = "cart.updated", groupId = "user-service-cart-group")
    public Mono<Void> handleCartUpdated(CartUpdatedEvent event) {
        log.info("Cart updated for user: {}, total items: {}", event.userId(), event.totalItems());
        return evictCacheSafely(event.userId(), "cart.updated");
    }

    @KafkaListener(topics = "cart.abandoned", groupId = "user-service-cart-group")
    public Mono<Void> handleCartAbandoned(CartAbandonedEvent event) {
        log.info("Cart abandoned for user: {}", event.userId());
        return evictCacheSafely(event.userId(), "cart.abandoned");
    }

    private Mono<Void> evictCacheSafely(String userIdStr, String eventType) {
        if (userIdStr == null || userIdStr.isBlank()) {
            log.warn("Received {} event with empty userId, skipping.", eventType);
            return Mono.empty();
        }

        try {
            UserId userId = new UserId(UUID.fromString(userIdStr));

            return cacheService.evictUserDashboardCache(userId)
                    .doOnSuccess(v -> log.debug("Successfully evicted dashboard cache for user: {} due to {}", userId, eventType))
                    .onErrorResume(e -> {
                        log.error("Failed to evict cache for user: {} during {}", userId, eventType, e);
                        return Mono.empty();
                    });
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in {} event: '{}'", eventType, userIdStr, e);
            return Mono.empty();
        }
    }
}
