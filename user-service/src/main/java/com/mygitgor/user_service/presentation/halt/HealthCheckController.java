package com.mygitgor.user_service.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
public class HealthCheckController {

    @GetMapping("/health")
    public Mono<Map<String, String>> health() {
        log.debug("Health check requested");
        return Mono.just(Map.of(
                "status", "UP",
                "service", "user-service",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @GetMapping("/ready")
    public Mono<Map<String, String>> ready() {
        return Mono.just(Map.of("status", "READY"));
    }

    @GetMapping("/live")
    public Mono<Map<String, String>> live() {
        return Mono.just(Map.of("status", "ALIVE"));
    }
}
