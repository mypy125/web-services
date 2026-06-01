package com.mygitgor.auth_service.infrastrucrure.cache;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class TokenCacheService {
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklisted_token:";
    private static final String ACTIVE_TOKEN_PREFIX = "active_token:";

    public Mono<Void> blacklistToken(String token, LocalDateTime expiresAt) {
        long ttlSeconds = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();

        if (ttlSeconds <= 0) {
            log.warn("Token already expired, not blacklisting: {}", token);
            return Mono.empty();
        }

        String key = BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.opsForValue()
                .set(key, true, Duration.ofSeconds(ttlSeconds))
                .doOnSuccess(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        log.debug("Token blacklisted: {}", token);
                    }
                })
                .doOnError(error -> log.error("Failed to blacklist token: {}", token, error))
                .then();
    }

    public Mono<Boolean> isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.hasKey(key)
                .doOnSuccess(exists -> {
                    if (exists) {
                        log.debug("Token is blacklisted: {}", token);
                    }
                })
                .doOnError(error -> log.error("Failed to check blacklist for token: {}", token, error))
                .onErrorReturn(false);
    }

    public Mono<Void> cacheActiveToken(String email, Map<String, Object> tokenInfo, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            log.warn("Invalid TTL for token cache: {} seconds", ttlSeconds);
            return Mono.empty();
        }

        String key = ACTIVE_TOKEN_PREFIX + email;
        return reactiveRedisTemplate.opsForValue()
                .set(key, tokenInfo, Duration.ofSeconds(ttlSeconds))
                .doOnSuccess(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        log.debug("Active token cached for email: {}", email);
                    }
                })
                .doOnError(error -> log.error("Failed to cache active token for email: {}", email, error))
                .then();
    }

    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getActiveToken(String email) {
        String key = ACTIVE_TOKEN_PREFIX + email;
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .map(value -> {
                    if (value instanceof Map) {
                        return (Map<String, Object>) value;
                    }
                    return null;
                })
                .doOnSuccess(tokenInfo -> {
                    if (tokenInfo != null) {
                        log.debug("Active token found for email: {}", email);
                    } else {
                        log.debug("No active token found for email: {}", email);
                    }
                })
                .doOnError(error -> log.error("Failed to get active token for email: {}", email, error))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Boolean> removeActiveToken(String email) {
        String key = ACTIVE_TOKEN_PREFIX + email;
        return reactiveRedisTemplate.delete(key)
                .map(deletedCount -> deletedCount > 0)
                .doOnSuccess(deleted -> {
                    if (deleted) {
                        log.debug("Active token removed for email: {}", email);
                    }
                })
                .doOnError(error -> log.error("Failed to remove active token for email: {}", email, error))
                .onErrorReturn(false);
    }

    public Mono<Boolean> removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.delete(key)
                .map(deletedCount -> deletedCount > 0)
                .doOnSuccess(deleted -> {
                    if (deleted) {
                        log.debug("Token removed from blacklist: {}", token);
                    }
                })
                .onErrorReturn(false);
    }

    public Mono<Boolean> refreshActiveTokenTTL(String email, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            return Mono.just(false);
        }

        String key = ACTIVE_TOKEN_PREFIX + email;
        return reactiveRedisTemplate.expire(key, Duration.ofSeconds(ttlSeconds))
                .doOnSuccess(refreshed -> {
                    if (Boolean.TRUE.equals(refreshed)) {
                        log.debug("Active token TTL refreshed for email: {}", email);
                    }
                })
                .onErrorReturn(false);
    }

}