package com.mygitgor.seller_service.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.statistic.SellerStatistics;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.page.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;
import java.time.Duration;


@Slf4j
@Service
@RequiredArgsConstructor
public class SellerCacheService {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SELLER_BY_ID_PREFIX = "seller:id:";
    private static final String SELLER_BY_EMAIL_PREFIX = "seller:email:";
    private static final String SELLER_AUTH_PREFIX = "seller:auth:";
    private static final String GLOBAL_STATISTICS_KEY = "seller:stats:global";
    private static final String SELLER_LIST_PREFIX = "seller:list:";

    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final Duration AUTH_TTL = Duration.ofMinutes(30);
    private static final Duration STATISTICS_TTL = Duration.ofMinutes(10);
    private static final Duration SELLER_LIST_TTL = Duration.ofMinutes(10);


    public Mono<Seller> getCachedSellerById(SellerId sellerId) {
        String key = SELLER_BY_ID_PREFIX + sellerId.getValue().toString();
        log.debug("Getting cached seller by ID: {}", sellerId);

        return redisTemplate.opsForValue().get(key)
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, Seller.class))
                        .doOnNext(u -> log.debug("Seller found in cache by ID: {}", sellerId))
                        .onErrorResume(e -> {
                            log.error("Failed to deserialize seller from cache for ID: {}", sellerId, e);
                            return Mono.empty(); // Безопасный фолбек при битом JSON
                        }));
    }

    public Mono<SellerStatistics> getCachedGlobalStatistics() {
        return redisTemplate.opsForValue().get(GLOBAL_STATISTICS_KEY)
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, SellerStatistics.class))
                        .onErrorResume(e -> {
                            log.error("Failed to deserialize global seller statistics", e);
                            return Mono.empty();
                        }));
    }

    public Mono<Page<Seller>> getCachedSellerList(String searchTerm, int page, int size) {
        String key = SELLER_LIST_PREFIX + searchTerm + ":" + page + ":" + size;

        return redisTemplate.opsForValue().get(key)
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, new TypeReference<Page<Seller>>() {}))
                        .onErrorResume(e -> {
                            log.error("Failed to deserialize seller list cache for key: {}", key, e);
                            return Mono.empty();
                        }));
    }

    public Mono<Void> cacheSellerById(Seller seller) {
        String key = SELLER_BY_ID_PREFIX + seller.getSellerId().toString();

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(seller))
                .flatMap(json -> redisTemplate.opsForValue().set(key, json, DEFAULT_TTL))
                .doOnError(error -> log.error("Failed to cache seller by ID: {}", seller.getSellerId().toString(), error))
                .then();
    }

    public Mono<Void> cacheGlobalStatistics(SellerStatistics statistics) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(statistics))
                .flatMap(json -> redisTemplate.opsForValue().set(GLOBAL_STATISTICS_KEY, json, STATISTICS_TTL))
                .then();
    }

    public Mono<Void> cacheSellerList(String searchTerm, int page, int size, Page<Seller> pageData) {
        String key = SELLER_LIST_PREFIX + searchTerm + ":" + page + ":" + size;

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(pageData))
                .flatMap(json -> redisTemplate.opsForValue().set(key, json, SELLER_LIST_TTL))
                .doOnError(e -> log.error("Failed to serialize seller list cache for key: {}", key, e))
                .then();
    }


    public Mono<Void> evictSellerById(SellerId sellerId) {
        return redisTemplate.delete(SELLER_BY_ID_PREFIX + sellerId.getValue().toString()).then();
    }

    public Mono<Void> evictSellerByEmail(Email email) {
        return redisTemplate.delete(SELLER_BY_EMAIL_PREFIX + email.value()).then();
    }

    public Mono<Void> evictAllSellerCaches(Seller seller) {
        log.debug("Evicting all caches for seller: {}", seller.getSellerId());
        return Mono.when(
                evictSellerById(seller.getSellerId()),
                evictSellerByEmail(seller.getEmail()),
                redisTemplate.delete(SELLER_AUTH_PREFIX + seller.getEmail().value())
        );
    }

    public Mono<Void> evictUserDashboardCache(SellerId sellerId) {
        if (sellerId == null) {
            return Mono.empty();
        }

        String dashboardKey = "seller:dashboard:" + sellerId.toString();
        String globalStatsKey = "seller:stats:global";

        log.debug("Evicting cache for seller dashboard: {} and global statistics", dashboardKey);

        return redisTemplate.delete(dashboardKey, globalStatsKey)
                .doOnSuccess(count -> log.trace("Successfully evicted {} cache keys from Redis", count))
                .doOnError(err -> log.error("Failed to evict cache keys from Redis for seller: {}", sellerId, err))
                .then();
    }
}