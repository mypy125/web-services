package com.mygitgor.user_service.infrastructure.persistence;

import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.domain.repository.UserStatisticsRepositoryPort;
import com.mygitgor.user_service.infrastructure.persistence.mapper.UserStatisticsPersistenceMapper;
import com.mygitgor.user_service.infrastructure.persistence.repository.UserStatisticsR2dbcRepository;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserStatisticsRepositoryAdapter implements UserStatisticsRepositoryPort {
    private final UserStatisticsR2dbcRepository repository;
    private final UserStatisticsPersistenceMapper mapper;

    @Override
    public Mono<UserStatistics> findByUserId(UserId userId) {
        return null;
    }

    @Override
    public Mono<UserStatistics> save(UserStatistics statistics) {
        return null;
    }

    @Override
    public Mono<UserStatistics> updateOrderStats(UserId userId, Double orderAmount, LocalDateTime orderDate) {
        return null;
    }

    @Override
    public Mono<UserStatistics> updateReviewStats(UserId userId, Integer rating) {
        return null;
    }

    @Override
    public Mono<UserStatistics> updateProductStats(UserId userId, String productId, String productName, String category) {
        return null;
    }

    @Override
    public Mono<UserStatistics> updateActivity(UserId userId) {
        return null;
    }

    @Override
    public Mono<UserStatistics> updateCouponStats(UserId userId, Double discountAmount) {
        return null;
    }

    @Override
    public Mono<UserStatistics> updateLoyaltyPoints(UserId userId, Integer points) {
        return null;
    }

    @Override
    public Mono<Void> deleteByUserId(UserId userId) {
        return null;
    }
}
