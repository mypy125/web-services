package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.domain.port.outgoing.UserStatisticsRepositoryPort;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateOrderStatsRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateReviewStatsRequest;
import com.mygitgor.user_service.infrastructure.dto.response.LoyaltyInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserStatisticsResponse;
import com.mygitgor.user_service.infrastructure.mapper.UserStatisticsMapper;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatisticsService {
    private final UserStatisticsRepositoryPort statisticsRepository;
    private final UserStatisticsMapper statisticsMapper;
    private final UserStatisticsEventProducer eventProducer;

    public Mono<UserStatisticsResponse> getStatistics(UserId userId) {
        log.debug("Getting statistics for user: {}", userId);

        return statisticsRepository.findByUserId(userId)
                .switchIfEmpty(Mono.fromCallable(() -> UserStatistics.create(userId))
                        .flatMap(statisticsRepository::save))
                .map(statisticsMapper::toResponse);
    }

    public Mono<UserStatisticsResponse> updateOrderStats(UserId userId, UpdateOrderStatsRequest request) {
        log.info("Updating order stats for user: {}", userId);

        return statisticsRepository.updateOrderStats(userId, request.getOrderAmount(), request.getOrderDate())
                .doOnSuccess(stats -> {
                    eventProducer.sendOrderStatsUpdatedEvent(userId, stats).subscribe();
                })
                .map(statisticsMapper::toResponse);
    }

    public Mono<UserStatisticsResponse> updateReviewStats(UserId userId, UpdateReviewStatsRequest request) {
        log.info("Updating review stats for user: {}", userId);

        return statisticsRepository.updateReviewStats(userId, request.getRating())
                .map(statisticsMapper::toResponse);
    }

    public Mono<UserStatisticsResponse> updateActivity(UserId userId) {
        log.debug("Updating activity for user: {}", userId);

        return statisticsRepository.updateActivity(userId)
                .map(statisticsMapper::toResponse);
    }

    public Mono<LoyaltyInfoResponse> getLoyaltyInfo(UserId userId) {
        log.debug("Getting loyalty info for user: {}", userId);

        return statisticsRepository.findByUserId(userId)
                .switchIfEmpty(Mono.fromCallable(() -> UserStatistics.create(userId))
                        .flatMap(statisticsRepository::save))
                .map(statisticsMapper::toLoyaltyInfo);
    }
}
