package com.mygitgor.user_service.domain.repository;

import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface UserStatisticsRepositoryPort {
    Mono<UserStatistics> findByUserId(UserId userId);
    Mono<UserStatistics> save(UserStatistics statistics);
    Mono<UserStatistics> updateOrderStats(UserId userId, Double orderAmount, LocalDateTime orderDate);
    Mono<UserStatistics> updateReviewStats(UserId userId, Integer rating);
    Mono<UserStatistics> updateProductStats(UserId userId, String productId, String productName, String category);
    Mono<UserStatistics> updateActivity(UserId userId);
    Mono<UserStatistics> updateCouponStats(UserId userId, Double discountAmount);
    Mono<UserStatistics> updateLoyaltyPoints(UserId userId, Integer points);
    Mono<Void> deleteByUserId(UserId userId);
}
