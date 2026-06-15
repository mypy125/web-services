package com.mygitgor.user_service.application.service.userstatistic;

import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.domain.repository.UserStatisticsRepositoryPort;
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
public class UserStatisticsQueryService {
    private final UserStatisticsRepositoryPort statisticsRepository;
    private final UserStatisticsMapper statisticsMapper;

    public Mono<UserStatisticsResponse> getStatistics(UserId userId) {
        log.debug("Query: Getting statistics for user: {}", userId);
        return findOrCreateStatistics(userId)
                .map(statisticsMapper::toResponse);
    }

    public Mono<LoyaltyInfoResponse> getLoyaltyInfo(UserId userId) {
        log.debug("Query: Getting loyalty info for user: {}", userId);
        return findOrCreateStatistics(userId)
                .map(statisticsMapper::toLoyaltyInfo);
    }

    private Mono<UserStatistics> findOrCreateStatistics(UserId userId) {
        return statisticsRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Statistics not found for user: {}, creating default", userId);
                    return statisticsRepository.save(UserStatistics.create(userId));
                }));
    }
}
