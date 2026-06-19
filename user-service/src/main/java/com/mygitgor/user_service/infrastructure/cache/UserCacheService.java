package com.mygitgor.user_service.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.infrastructure.dto.response.UserResponse;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Page;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_BY_ID_PREFIX = "user:id:";
    private static final String USER_BY_EMAIL_PREFIX = "user:email:";
    private static final String USER_AUTH_PREFIX = "user:auth:";
    private static final String USER_STATISTICS_PREFIX = "user:stats:";
    private static final String USER_LIST_PREFIX = "user:list:";

    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final Duration AUTH_TTL = Duration.ofMinutes(30);
    private static final Duration STATISTICS_TTL = Duration.ofHours(6);
    private static final Duration USER_LIST_TTL = Duration.ofMinutes(10);


    public Mono<User> getCachedUserById(UserId userId) {
        String key = USER_BY_ID_PREFIX + userId.getValue().toString();
        log.debug("Getting cached user by ID: {}", userId);

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> {
                    try {
                        User user = objectMapper.readValue(json, User.class);
                        log.debug("User found in cache by ID: {}", userId);
                        return Mono.just(user);
                    } catch (Exception e) {
                        log.error("Failed to deserialize user from cache for ID: {}", userId, e);
                        return Mono.empty();
                    }
                });
    }

    public Mono<Void> cacheUserById(User user) {
        String key = USER_BY_ID_PREFIX + user.getId().getValue().toString();
        log.debug("Caching user by ID: {}", user.getId());

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(user))
                .flatMap(json -> redisTemplate.opsForValue().set(key, json, DEFAULT_TTL))
                .doOnError(error -> log.error("Failed to cache user by ID: {}", user.getId(), error))
                .then();
    }

    public Mono<Void> evictUserById(UserId userId) {
        String key = USER_BY_ID_PREFIX + userId.getValue().toString();
        return redisTemplate.delete(key).then();
    }

    public Mono<User> getCachedUserByEmail(Email email) {
        String key = USER_BY_EMAIL_PREFIX + email.value();
        log.debug("Getting cached user by email: {}", email);

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> {
                    try {
                        User user = objectMapper.readValue(json, User.class);
                        return Mono.just(user);
                    } catch (Exception e) {
                        log.error("Failed to deserialize user from cache by email: {}", email, e);
                        return Mono.empty();
                    }
                });
    }

    public Mono<Void> cacheUserByEmail(User user) {
        String key = USER_BY_EMAIL_PREFIX + user.getEmail().value();
        log.debug("Caching user by email: {}", user.getEmail());

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(user))
                .flatMap(json -> redisTemplate.opsForValue().set(key, json, DEFAULT_TTL))
                .then();
    }

    public Mono<Void> evictUserByEmail(Email email) {
        String key = USER_BY_EMAIL_PREFIX + email.value();
        return redisTemplate.delete(key).then();
    }

    public Mono<Map<String, Object>> getCachedAuthInfo(String email) {
        String key = USER_AUTH_PREFIX + email;
        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = objectMapper.readValue(json, Map.class);
                        return Mono.just(map);
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                });
    }

    public Mono<Void> cacheAuthInfo(String email, String userId, String role, boolean emailVerified, String accountStatus) {
        String key = USER_AUTH_PREFIX + email;

        return Mono.fromCallable(() -> {
                    Map<String, Object> authMap = Map.of(
                            "userId", userId,
                            "email", email,
                            "role", role,
                            "emailVerified", emailVerified,
                            "accountStatus", accountStatus,
                            "cachedAt", LocalDateTime.now().toString()
                    );
                    return objectMapper.writeValueAsString(authMap);
                })
                .flatMap(json -> redisTemplate.opsForValue().set(key, json, AUTH_TTL))
                .then();
    }

    public Mono<Void> evictAuthInfo(String email) {
        return redisTemplate.delete(USER_AUTH_PREFIX + email).then();
    }

    public Mono<Map<String, Object>> getCachedStatistics(UserId userId) {
        String key = USER_STATISTICS_PREFIX + userId.getValue().toString();
        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = objectMapper.readValue(json, Map.class);
                        return Mono.just(map);
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                });
    }

    public Mono<Void> cacheStatistics(UserId userId, Map<String, Object> statistics) {
        String key = USER_STATISTICS_PREFIX + userId.getValue().toString();

        return Mono.fromCallable(() -> {
                    return objectMapper.writeValueAsString(statistics);
                })
                .flatMap(json -> redisTemplate.opsForValue().set(key, json, STATISTICS_TTL))
                .then();
    }

    public Mono<Page<UserResponse>> getCachedUserList(String searchTerm, int page, int size) {
        String key = "user:search:" + searchTerm + ":" + page + ":" + size;

        return redisTemplate.opsForValue().get(key)
                .map(json -> {
                    try {
                        return objectMapper.readValue(
                                json,
                                new TypeReference<Page<UserResponse>>() {}
                        );
                    } catch (Exception e) {
                        log.error("Failed to deserialize user list cache for key: {}", key, e);
                        return null;
                    }
                });
    }

    public Mono<Void> cacheUserList(String searchTerm, int page, int size, Map<String, Object> listData) {
        String key = USER_LIST_PREFIX + searchTerm + ":" + page + ":" + size;
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(listData))
                .flatMap(json -> redisTemplate.opsForValue().set(key, json, USER_LIST_TTL))
                .then();
    }

    public Mono<Void> evictAllUserCaches(User user) {
        log.debug("Evicting all caches for user: {}", user.getId());

        return Mono.when(
                evictUserById(user.getId()),
                evictUserByEmail(user.getEmail()),
                evictAuthInfo(user.getEmail().toString())
        );
    }

    public Mono<Void> refreshUserCache(User user) {
        log.debug("Refreshing cache for user: {}", user.getId());
        return evictAllUserCaches(user)
                .then(Mono.when(
                        cacheUserById(user),
                        cacheUserByEmail(user)
                ));
    }

    public Mono<Void> evictStatistics(UserId userId) {
        String key = USER_STATISTICS_PREFIX + userId.getValue().toString();
        log.debug("Evicting statistics cache for user: {}", userId);
        return redisTemplate.delete(key).then();
    }

    public Mono<Void> cacheUserList(String searchTerm, int page, int size, Page<UserResponse> pageResponse) {
        String key = "user:search:" + searchTerm + ":" + page + ":" + size;

        try {
            String json = objectMapper.writeValueAsString(pageResponse);
            return redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(10)).then();
        } catch (Exception e) {
            log.error("Failed to serialize user list cache for key: {}", key, e);
            return Mono.empty();
        }
    }

    public Mono<Page<User>> getCachedDomainUserList(String searchTerm, int page, int size) {
        String key = "user:search:domain:" + searchTerm + ":" + page + ":" + size;

        return redisTemplate.opsForValue().get(key)
                .map(json -> {
                    try {
                        return objectMapper.readValue(
                                json,
                                new TypeReference<Page<User>>() {}
                        );
                    } catch (Exception e) {
                        log.error("Failed to deserialize domain user list cache for key: {}", key, e);
                        return null;
                    }
                });
    }

    public Mono<Void> evictUserDashboardCache(UserId userId) {
        String dashboardKey = "user:dashboard:" + userId.getValue();
        String profileKey = "user:profile:" + userId.getValue();

        return redisTemplate.delete(dashboardKey)
                .then(redisTemplate.delete(profileKey))
                .then();
    }

    public Mono<Void> cacheDomainUserList(String searchTerm, int page, int size, Page<User> pageData) {
        String key = "user:search:domain:" + searchTerm + ":" + page + ":" + size;

        try {
            String json = objectMapper.writeValueAsString(pageData);
            return redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(10)).then();
        } catch (Exception e) {
            log.error("Failed to serialize domain user list cache for key: {}", key, e);
            return Mono.empty();
        }
    }
}
