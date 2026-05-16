package com.mygitgor.auth_service.infrastrucrure.persistance.repository;

import com.mygitgor.auth_service.infrastrucrure.persistance.entity.TokenEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TokenR2dbcRepository extends ReactiveCrudRepository<TokenEntity, UUID> {
    Mono<TokenEntity> findByValue(String value);
    Flux<TokenEntity> findAllByEmail(String email);
    Flux<TokenEntity> findAllByUserId(String userId);
    Mono<Void> deleteByValue(String value);
    Mono<Void> deleteAllByEmail(String email);
    Mono<Void> deleteAllByUserId(String userId);
    Mono<TokenEntity> findFirstByUserIdAndStatusOrderByIssuedAtDesc(String userId, String status);
    Flux<TokenEntity> findAllByUserIdAndStatus(String userId, String status);
    Mono<Boolean> existsByValue(String value);
    Mono<Boolean> existsByTokenAndStatus(String value, String status);
}
