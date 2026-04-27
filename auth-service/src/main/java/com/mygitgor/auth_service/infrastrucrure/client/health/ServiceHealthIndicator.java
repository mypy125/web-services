package com.mygitgor.auth_service.infrastrucrure.client.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ServiceHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<Health> health() {
        return checkUserService()
                .zipWith(checkSellerService())
                .zipWith(checkCartService())
                .map(tuple -> {
                    Health.Builder builder = Health.up();
                    builder.withDetail("userService", tuple.getT1().getStatus().getCode());
                    builder.withDetail("sellerService", tuple.getT2().getStatus().getCode());
                    builder.withDetail("cartService", tuple.getT3().getStatus().getCode());
                    return builder.build();
                })
                .onErrorResume(e -> Mono.just(Health.down().withException(e).build()));
    }

    private Mono<Health> checkUserService() {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8082/api/users/health")
                .retrieve()
                .toBodilessEntity()
                .map(response -> Health.up().build())
                .onErrorResume(e -> Mono.just(Health.down().withDetail("error", e.getMessage()).build()));
    }

    private Mono<Health> checkSellerService() {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/api/sellers/health")
                .retrieve()
                .toBodilessEntity()
                .map(response -> Health.up().build())
                .onErrorResume(e -> Mono.just(Health.down().withDetail("error", e.getMessage()).build()));
    }

    private Mono<Health> checkCartService() {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8084/api/carts/health")
                .retrieve()
                .toBodilessEntity()
                .map(response -> Health.up().build())
                .onErrorResume(e -> Mono.just(Health.down().withDetail("error", e.getMessage()).build()));
    }
}
