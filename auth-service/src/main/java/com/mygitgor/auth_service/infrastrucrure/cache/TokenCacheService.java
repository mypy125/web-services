package com.mygitgor.auth_service.infrastrucrure.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

@Getter
@Service
@RequiredArgsConstructor
public class TokenCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklisted_token:";
    private static final String ACTIVE_TOKEN_PREFIX = "active_token:";

    public void blacklistToken(String token, LocalDateTime expiresAt) {
        long ttl = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, true, ttl, TimeUnit.SECONDS);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        Boolean exists = (Boolean) redisTemplate.opsForValue().get(BLACKLIST_PREFIX + token);
        return Boolean.TRUE.equals(exists);
    }

    public void cacheActiveToken(String email, Map<String, Object> tokenInfo, long ttlSeconds) {
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(ACTIVE_TOKEN_PREFIX + email, tokenInfo, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    public Map<String, Object> getActiveToken(String email) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenInfo = (Map<String, Object>) redisTemplate.opsForValue().get(ACTIVE_TOKEN_PREFIX + email);
            return tokenInfo;
        } catch (Exception e) {
            return null;
        }
    }

    public void removeActiveToken(String email) {
        redisTemplate.delete(ACTIVE_TOKEN_PREFIX + email);
    }

}