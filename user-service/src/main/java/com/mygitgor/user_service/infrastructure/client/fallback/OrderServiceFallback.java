package com.mygitgor.user_service.infrastructure.client.fallback;

import com.mygitgor.user_service.infrastructure.dto.external.OrderStatisticsDto;
import com.mygitgor.user_service.infrastructure.dto.external.OrderSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OrderServiceFallback {

    public Flux<OrderSummaryDto> getUserOrders(String userId, int page, int size) {
        log.warn("Fallback: Returning empty orders for user: {}, page: {}, size: {}", userId, page, size);
        return Flux.empty();
    }

    public Mono<OrderStatisticsDto> getOrderStatistics(String userId) {
        log.warn("Fallback: Returning empty order statistics for user: {}", userId);
        return Mono.empty();
    }
}
