package com.mygitgor.user_service.application.service.userstatistic;

import com.mygitgor.user_service.domain.repository.UserStatisticsRepositoryPort;
import com.mygitgor.user_service.infrastructure.dto.request.*;
import com.mygitgor.user_service.infrastructure.dto.response.UserStatisticsResponse;
import com.mygitgor.user_service.infrastructure.kafka.producer.UserStatisticsEventProducer;
import com.mygitgor.user_service.infrastructure.mapper.UserStatisticsMapper;
import com.mygitgor.user_service.shared.exception.UserNotFoundException;
import com.mygitgor.user_service.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatisticsCommandService {
    private final UserStatisticsRepositoryPort statisticsRepository;
    private final UserStatisticsMapper statisticsMapper;
    private final UserStatisticsEventProducer eventProducer;

    @Transactional
    public Mono<UserStatisticsResponse> updateOrderStats(UserId userId, UpdateOrderStatsRequest request) {
        log.info("Updating order stats for user: {}", userId);

        return statisticsRepository.updateOrderStats(userId, request.orderAmount(), request.orderDate())
                .delayUntil(stats -> eventProducer.sendOrderStatsUpdatedEvent(userId, stats)
                        .doOnSuccess(v -> log.debug("Order stats event sent to Kafka for user: {}", userId))
                        .doOnError(e -> log.error("Failed to send order stats event for user: {}", userId, e)))
                .map(statisticsMapper::toResponse);
    }

    @Transactional
    public Mono<UserStatisticsResponse> updateReviewStats(UserId userId, UpdateReviewStatsRequest request) {
        log.info("Updating review stats for user: {}", userId);

        return statisticsRepository.updateReviewStats(userId, request.rating())
                .switchIfEmpty(Mono.error(new UserNotFoundException("Statistics not found for user: " + userId)))
                .map(statisticsMapper::toResponse);
    }

    @Transactional
    public Mono<UserStatisticsResponse> updateActivity(UserId userId) {
        log.debug("Updating activity for user: {}", userId);

        return statisticsRepository.updateActivity(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Statistics not found for user: " + userId)))
                .map(statisticsMapper::toResponse);
    }

    @Transactional
    public Mono<UserStatisticsResponse> updateProductStats(UserId userId, UpdateProductStatsRequest request) {
        log.info("Command: Updating purchased product stats for user: {}", userId);

        return statisticsRepository.updateProductStats(
                        userId,
                        request.productId(),
                        request.productName(),
                        request.category()
                )
                .map(statisticsMapper::toResponse);
    }

    @Transactional
    public Mono<UserStatisticsResponse> updateCouponStats(UserId userId, UpdateCouponStatsRequest request) {
        log.info("Command: Updating coupon and discount stats for user: {}", userId);

        return statisticsRepository.updateCouponStats(userId, request.discountAmount())
                .map(statisticsMapper::toResponse);
    }

    @Transactional
    public Mono<UserStatisticsResponse> updateLoyaltyPoints(UserId userId, UpdateLoyaltyPointsRequest request) {
        log.info("Command: Direct update of loyalty points for user: {}", userId);

        return statisticsRepository.updateLoyaltyPoints(userId, request.points())
                .map(statisticsMapper::toResponse);
    }

    @Transactional
    public Mono<Void> deleteStatistics(UserId userId) {
        log.warn("Command: Permanent deletion of statistics requested for user: {}", userId);
        return statisticsRepository.deleteByUserId(userId);
    }
}
