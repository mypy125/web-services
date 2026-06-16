package com.mygitgor.user_service.presentation.controller;

import com.mygitgor.user_service.application.service.userstatistic.UserStatisticsQueryService;
import com.mygitgor.user_service.infrastructure.dto.response.LoyaltyInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserStatisticsResponse;
import com.mygitgor.user_service.infrastructure.security.AuthUser;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Statistics", description = "Endpoints for user gamification and loyalty statistics")
public class UserStatisticsController {
    private final UserStatisticsQueryService statisticsQueryService;

    @GetMapping("/me/statistics")
    @Operation(summary = "Get current user statistics",
            description = "Fetches order counts, reviews, and activity metrics for the authenticated user.")
    public Mono<UserStatisticsResponse> getMyStatistics(Authentication authentication) {
        log.debug("REST request to get statistics for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();

        return statisticsQueryService.getStatistics(new UserId(userId));
    }

    @GetMapping("/me/loyalty")
    @Operation(summary = "Get current user loyalty info",
            description = "Fetches current loyalty level, accumulated discount, and spending stats.")
    public Mono<LoyaltyInfoResponse> getMyLoyaltyInfo(Authentication authentication) {
        log.debug("REST request to get loyalty info for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();

        return statisticsQueryService.getLoyaltyInfo(new UserId(userId));
    }
}
