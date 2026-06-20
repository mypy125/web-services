package com.mygitgor.user_service.presentation.controller;

import com.mygitgor.user_service.application.service.UserDashboardService;
import com.mygitgor.user_service.infrastructure.dto.external.OrderStatisticsDto;
import com.mygitgor.user_service.infrastructure.dto.external.UserDashboardDto;
import com.mygitgor.user_service.infrastructure.dto.external.UserDashboardSummaryDto;
import com.mygitgor.user_service.infrastructure.security.AuthUser;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/dashboard")
@RequiredArgsConstructor
@Tag(name = "User Dashboard", description = "Endpoints for fetching aggregated user analytics, profile summaries, and statistics")
public class UserDashboardController {
    private final UserDashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get full user dashboard",
            description = "Aggregates data from multiple services: profile, cart, orders, coupons, addresses")
    public Mono<UserDashboardDto> getDashboard(Authentication authentication) {
        String userId = extractUserId(authentication);
        log.debug("Getting full dashboard for user: {}", userId);
        return dashboardService.getUserDashboard(new UserId(userId));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary",
            description = "Lightweight dashboard summary with key counters and metrics")
    public Mono<UserDashboardSummaryDto> getDashboardSummary(Authentication authentication) {
        String userId = extractUserId(authentication);
        log.debug("Getting dashboard summary for user: {}", userId);
        return dashboardService.getDashboardSummary(new UserId(userId));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get detailed order statistics",
            description = "Returns complex analytical data regarding user lifetime orders, spending growth, and categories")
    public Mono<OrderStatisticsDto> getOrderStatistics(Authentication authentication) {
        String userId = extractUserId(authentication);
        log.debug("Getting order statistics for user: {}", userId);
        return dashboardService.getOrderStatistics(new UserId(userId));
    }

    private String extractUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser)) {
            throw new IllegalStateException("Authentication principal is missing or invalid");
        }
        return ((AuthUser) authentication.getPrincipal()).getUserId();
    }
}