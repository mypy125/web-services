package com.mygitgor.user_service.infrastructure.persistence;

import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.domain.repository.UserStatisticsRepositoryPort;
import com.mygitgor.user_service.infrastructure.persistence.entity.UserStatisticsEntity;
import com.mygitgor.user_service.infrastructure.persistence.mapper.UserStatisticsPersistenceMapper;
import com.mygitgor.user_service.infrastructure.persistence.repository.UserStatisticsR2dbcRepository;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
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
        return repository.findByUserId(UUID.fromString(userId.toString()))
                .map(mapper::toDomain)
                .doOnSuccess(stats -> {
                    if (stats != null) {
                        log.debug("Statistics found for user: {}", userId);
                    } else {
                        log.debug("No statistics found for user: {}", userId);
                    }
                })
                .doOnError(error -> log.error("Failed to find statistics for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<UserStatistics> save(UserStatistics statistics) {
        log.debug("Saving statistics for user: {}", statistics.getUserId());
        return repository.findByUserId(UUID.fromString(statistics.getUserId().toString()))
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

        return repository.findByUserId(UUID.fromString(userId.toString()))
                .flatMap(entity -> {
                    int newTotalOrders = (entity.getTotalOrders() != null ? entity.getTotalOrders() : 0) + 1;
                    double newTotalSpent = (entity.getTotalSpent() != null ? entity.getTotalSpent() : 0.0) + orderAmount;
                    double newAvgOrderValue = newTotalSpent / newTotalOrders;

                    entity.setTotalOrders(newTotalOrders);
                    entity.setTotalSpent(newTotalSpent);
                    entity.setAverageOrderValue(newAvgOrderValue);
                    entity.setLastOrderDate(orderDate);
                    entity.setUpdatedAt(LocalDateTime.now());

                    return repository.save(entity);
                })
                .map(mapper::toDomain)
                .doOnSuccess(stats -> log.debug("Order stats updated for user: {}", userId))
                .doOnError(error -> log.error("Failed to update order stats for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<UserStatistics> updateReviewStats(UserId userId, Integer rating) {
        log.debug("Updating review stats for user: {}, rating: {}", userId, rating);

        return repository.findByUserId(UUID.fromString(userId.toString()))
                .flatMap(entity -> {
                    int newTotalReviews = (entity.getTotalReviews() != null ? entity.getTotalReviews() : 0) + 1;
                    double currentTotalRating = (entity.getAverageRating() != null ? entity.getAverageRating() : 0.0) * (newTotalReviews - 1);
                    double newAverageRating = (currentTotalRating + rating) / newTotalReviews;

                    entity.setTotalReviews(newTotalReviews);
                    entity.setAverageRating(newAverageRating);
                    entity.setUpdatedAt(LocalDateTime.now());

                    return repository.save(entity);
                })
                .map(mapper::toDomain)
                .doOnSuccess(stats -> log.debug("Review stats updated for user: {}", userId))
                .doOnError(error -> log.error("Failed to update review stats for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<UserStatistics> updateProductStats(UserId userId, String productId, String productName, String category) {
        log.debug("Updating product stats for user: {}, product: {}, category: {}", userId, productId, category);

        return repository.findByUserId(UUID.fromString(userId.toString()))
                .flatMap(entity -> {
                    int newTotalProducts = (entity.getTotalProductsPurchased() != null ? entity.getTotalProductsPurchased() : 0) + 1;
                    entity.setTotalProductsPurchased(newTotalProducts);
                    entity.setFavoriteProductId(productId);
                    entity.setFavoriteProductName(productName);
                    entity.setMostPurchasedCategory(category);
                    entity.setUpdatedAt(LocalDateTime.now());

                    return repository.save(entity);
                })
                .map(mapper::toDomain)
                .doOnSuccess(stats -> log.debug("Product stats updated for user: {}", userId))
                .doOnError(error -> log.error("Failed to update product stats for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<UserStatistics> updateActivity(UserId userId) {
        log.debug("Updating activity for user: {}", userId);

        return repository.findByUserId(UUID.fromString(userId.toString()))
                .flatMap(entity -> {
                    int newDaysActive = (entity.getDaysActive() != null ? entity.getDaysActive() : 0) + 1;
                    entity.setLastActiveAt(LocalDateTime.now());
                    entity.setDaysActive(newDaysActive);
                    entity.setUpdatedAt(LocalDateTime.now());

                    return repository.save(entity);
                })
                .map(mapper::toDomain)
                .doOnSuccess(stats -> log.debug("Activity updated for user: {}", userId))
                .doOnError(error -> log.error("Failed to update activity for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<UserStatistics> updateCouponStats(UserId userId, Double discountAmount) {
        log.debug("Updating coupon stats for user: {}, discount: {}", userId, discountAmount);

        return repository.findByUserId(UUID.fromString(userId.toString()))
                .flatMap(entity -> {
                    int newCouponsUsed = (entity.getCouponsUsed() != null ? entity.getCouponsUsed() : 0) + 1;
                    double newTotalDiscount = (entity.getTotalDiscountReceived() != null ? entity.getTotalDiscountReceived() : 0.0) + discountAmount;

                    entity.setCouponsUsed(newCouponsUsed);
                    entity.setTotalDiscountReceived(newTotalDiscount);
                    entity.setUpdatedAt(LocalDateTime.now());

                    return repository.save(entity);
                })
                .map(mapper::toDomain)
                .doOnSuccess(stats -> log.debug("Coupon stats updated for user: {}", userId))
                .doOnError(error -> log.error("Failed to update coupon stats for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<UserStatistics> updateLoyaltyPoints(UserId userId, Integer points) {
        log.debug("Updating loyalty points for user: {}, points: {}", userId, points);

        return repository.findByUserId(UUID.fromString(userId.toString()))
                .flatMap(entity -> {
                    int newPoints = (entity.getLoyaltyPoints() != null ? entity.getLoyaltyPoints() : 0) + points;
                    entity.setLoyaltyPoints(newPoints);

                    String newTier;
                    if (newPoints >= 10000) {
                        newTier = "PLATINUM";
                    } else if (newPoints >= 5000) {
                        newTier = "GOLD";
                    } else if (newPoints >= 1000) {
                        newTier = "SILVER";
                    } else {
                        newTier = "BRONZE";
                    }
                    entity.setLoyaltyTier(newTier);
                    entity.setUpdatedAt(LocalDateTime.now());

                    return repository.save(entity);
                })
                .map(mapper::toDomain)
                .doOnSuccess(stats -> log.debug("Loyalty points updated for user: {}", userId))
                .doOnError(error -> log.error("Failed to update loyalty points for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<Void> deleteByUserId(UserId userId) {
        log.debug("Deleting statistics for user: {}", userId);
        return repository.deleteByUserId(UUID.fromString(userId.toString()))
                .doOnSuccess(v -> log.debug("Statistics deleted for user: {}", userId))
                .doOnError(error -> log.error("Failed to delete statistics for user {}: {}", userId, error.getMessage()));
    }
}
