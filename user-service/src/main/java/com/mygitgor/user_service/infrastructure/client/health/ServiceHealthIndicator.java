package com.mygitgor.user_service.infrastructure.client.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceHealthIndicator implements ReactiveHealthIndicator {
    @Override
    public Mono<Health> health() {
        return null;
    }
}
