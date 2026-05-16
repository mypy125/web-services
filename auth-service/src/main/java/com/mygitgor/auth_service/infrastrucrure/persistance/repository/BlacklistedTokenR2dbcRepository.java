package com.mygitgor.auth_service.infrastrucrure.persistance.repository;

import com.mygitgor.auth_service.infrastrucrure.persistance.entity.BlacklistedTokenEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BlacklistedTokenR2dbcRepository extends ReactiveCrudRepository<BlacklistedTokenEntity, UUID> {
    Mono<Boolean> existsByToken(String token);
    Mono<BlacklistedTokenEntity> findByToken(String token);
    Mono<Integer> deleteByExpiresAtBefore(LocalDateTime dateTime);
    Mono<Void> deleteByUserId(UUID userId);
    Mono<Long> countByExpiresAtAfter(LocalDateTime dateTime);
}
