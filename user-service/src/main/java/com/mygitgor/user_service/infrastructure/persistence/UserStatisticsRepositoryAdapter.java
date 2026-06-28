package com.mygitgor.user_service.infrastructure.persistence;

import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.domain.repository.UserStatisticsRepositoryPort;
import com.mygitgor.user_service.infrastructure.persistence.entity.UserStatisticsEntity;
import com.mygitgor.user_service.infrastructure.persistence.mapper.UserStatisticsPersistenceMapper;
import com.mygitgor.user_service.infrastructure.persistence.repository.UserStatisticsR2dbcRepository;
import com.mygitgor.user_service.shared.exception.UserNotFoundException;
import com.mygitgor.user_service.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserStatisticsRepositoryAdapter implements UserStatisticsRepositoryPort {
    private final UserStatisticsR2dbcRepository repository;
    private final UserStatisticsPersistenceMapper mapper;

    @Override
    public Mono<UserStatistics> findByUserId(UserId userId) {
        log.debug("Finding statistics by user ID: {}", userId);
        return repository.findByUserId(userId.getValue())
                .map(mapper::toDomain)
                .doOnError(error -> log.error("Failed to find statistics for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<UserStatistics> save(UserStatistics statistics) {
        log.debug("Saving statistics for user: {}", statistics.getUserId());
        return repository.findByUserId(statistics.getUserId().getValue())
                .flatMap(existingEntity -> {
                    UserStatisticsEntity entity = mapper.toEntity(statistics);
                    entity.setCreatedAt(existingEntity.getCreatedAt());
                    return repository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    UserStatisticsEntity entity = mapper.toEntity(statistics);
                    return repository.save(entity);
                }))
                .map(mapper::toDomain)
                .doOnSuccess(saved -> log.debug("Statistics saved for user: {}", statistics.getUserId()))
                .doOnError(error -> log.error("Failed to save statistics for user {}: {}", statistics.getUserId(), error.getMessage()));
    }

    @Override
    public Mono<UserStatistics> updateOrderStats(UserId userId, Double orderAmount, LocalDateTime orderDate) {
        log.debug("Updating order stats for user: {}, amount: {}, date: {}", userId, orderAmount, orderDate);

        return repository.findByUserId(userId.getValue())
                .switchIfEmpty(Mono.error(new UserNotFoundException("Stats not found")))
                .map(mapper::toDomain)
                .map(domain -> {
                    domain.updateOrderStats(orderAmount);
                    return domain;
                })
                .flatMap(this::save);
    }

    @Override
    public Mono<UserStatistics> updateReviewStats(UserId userId, Integer rating) {
        log.debug("Updating review stats for user: {}, rating: {}", userId, rating);

        return repository.findByUserId(userId.getValue())
                .switchIfEmpty(Mono.defer(() -> repository.save(mapper.toEntity(UserStatistics.create(userId)))))
                .map(mapper::toDomain)
                .map(domain -> {
                    domain.updateReviewStats(rating);
                    return domain;
                })
                .flatMap(this::save);
    }

    @Override
    public Mono<UserStatistics> updateProductStats(UserId userId, String productId, String productName, String category) {
        log.debug("Updating product stats for user: {}, product: {}, category: {}", userId, productId, category);

        return repository.findByUserId(userId.getValue())
                .switchIfEmpty(Mono.defer(() -> repository.save(mapper.toEntity(UserStatistics.create(userId)))))
                .map(mapper::toDomain)
                .map(domain -> {
                    domain.updateProductStats(category, productId, productName);
                    return domain;
                })
                .flatMap(this::save);
    }

    @Override
    public Mono<UserStatistics> updateActivity(UserId userId) {
        log.debug("Updating activity for user: {}", userId);

        return repository.findByUserId(userId.getValue())
                .switchIfEmpty(Mono.defer(() -> repository.save(mapper.toEntity(UserStatistics.create(userId)))))
                .map(mapper::toDomain)
                .map(domain -> {
                    domain.updateActivity();
                    return domain;
                })
                .flatMap(this::save);
    }

    @Override
    public Mono<UserStatistics> updateCouponStats(UserId userId, Double discountAmount) {
        log.debug("Updating coupon stats for user: {}, discount: {}", userId, discountAmount);

        return repository.findByUserId(userId.getValue())
                .switchIfEmpty(Mono.defer(() -> repository.save(mapper.toEntity(UserStatistics.create(userId)))))
                .map(mapper::toDomain)
                .map(domain -> {
                    domain.updateCouponUsage(discountAmount);
                    return domain;
                })
                .flatMap(this::save);
    }

    @Override
    public Mono<UserStatistics> updateLoyaltyPoints(UserId userId, Integer points) {
        log.debug("Updating loyalty points for user: {}, points: {}", userId, points);

        return repository.findByUserId(userId.getValue())
                .switchIfEmpty(Mono.defer(() -> repository.save(mapper.toEntity(UserStatistics.create(userId)))))
                .map(mapper::toDomain)
                .map(domain -> {
                    domain.updateLoyaltyPoints(points);
                    return domain;
                })
                .flatMap(this::save);
    }

    @Override
    public Mono<Void> deleteByUserId(UserId userId) {
        log.debug("Deleting statistics for user: {}", userId);
        return repository.deleteByUserId(UUID.fromString(userId.toString()))
                .doOnError(error -> log.error("Failed to delete statistics for user {}: {}", userId, error.getMessage()));
    }
}
