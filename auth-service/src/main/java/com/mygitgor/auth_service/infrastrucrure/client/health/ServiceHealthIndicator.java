package com.mygitgor.auth_service.infrastrucrure.client.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceHealthIndicator implements ReactiveHealthIndicator {
    private final WebClient.Builder webClientBuilder;
    private final Map<String, Health> healthCache = new ConcurrentHashMap<>();
    private final Duration cacheTtl = Duration.ofSeconds(30);
    private final Map<String, Instant> lastCheckTime = new ConcurrentHashMap<>();

    @Value("${user.service.url:http://localhost:8082/api/users}")
    private String userServiceUrl;

    @Value("${seller.service.url:http://localhost:8083/api/sellers}")
    private String sellerServiceUrl;

    @Value("${cart.service.url:http://localhost:8084/api/carts}")
    private String cartServiceUrl;

    @Value("${notification.service.url:http://localhost:8085/api/notifications}")
    private String notificationServiceUrl;

    @Override
    public Mono<Health> health() {
        return Mono.zip(
                checkServiceWithCache(userServiceUrl, "User Service"),
                checkServiceWithCache(sellerServiceUrl, "Seller Service"),
                checkServiceWithCache(cartServiceUrl, "Cart Service"),
                checkServiceWithCache(notificationServiceUrl, "Notification Service")
        ).map(tuple -> {
            Health.Builder builder = Health.up();

            addHealthDetail(builder, tuple.getT1());
            addHealthDetail(builder, tuple.getT2());
            addHealthDetail(builder, tuple.getT3());
            addHealthDetail(builder, tuple.getT4());

            return builder.build();
        });
    }

    private Mono<Health> checkServiceWithCache(String url, String serviceName) {
        Instant lastCheck = lastCheckTime.get(serviceName);

        if (lastCheck != null && lastCheck.plus(cacheTtl).isAfter(Instant.now())) {
            Health cachedHealth = healthCache.get(serviceName);
            if (cachedHealth != null) {
                log.debug("Using cached health for {}", serviceName);
                return Mono.just(cachedHealth);
            }
        }

        return checkService(url, serviceName)
                .doOnNext(health -> {
                    healthCache.put(serviceName, health);
                    lastCheckTime.put(serviceName, Instant.now());
                });
    }

    private Mono<Health> checkService(String url, String serviceName) {
        return webClientBuilder.build()
                .get()
                .uri(url + "/actuator/health")
                .retrieve()
                .toBodilessEntity()
                .map(response -> Health.up()
                        .withDetail("service", serviceName)
                        .withDetail("status", "UP")
                        .build())
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.warn("{} health check failed: {}", serviceName, e.getMessage());
                    return Mono.just(Health.down()
                            .withDetail("service", serviceName)
                            .withDetail("status", "DOWN")
                            .withDetail("error", e.getMessage())
                            .build());
                });
    }

    private void addHealthDetail(Health.Builder builder, Health health) {
        if (health != null && health.getDetails() != null) {
            builder.withDetail(
                    health.getDetails().get("service") + "_status",
                    health.getDetails().get("status")
            );
        }
    }
}
