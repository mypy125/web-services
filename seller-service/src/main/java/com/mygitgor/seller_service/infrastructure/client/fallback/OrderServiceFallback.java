package com.mygitgor.seller_service.infrastructure.client.fallback;

import com.mygitgor.seller_service.application.dto.external.OrderDetailsDto;
import com.mygitgor.seller_service.application.dto.external.OrderStatisticsDto;
import com.mygitgor.seller_service.application.dto.external.OrderSummaryDto;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OrderServiceFallback {

    public Mono<OrderDetailsDto> getOrderDetails(OrderId orderId) {
        log.warn("Fallback: Returning empty order details for: {}", orderId);
        return Mono.empty();
    }

    public Flux<OrderSummaryDto> getOrdersBySellerId(SellerId sellerId, int page, int size) {
        log.warn("Fallback: Returning empty orders for seller: {}", sellerId);
        return Flux.empty();
    }

    public Mono<OrderStatisticsDto> getOrderStatistics(SellerId sellerId) {
        log.warn("Fallback: Returning empty order statistics for seller: {}", sellerId);
        return Mono.empty();
    }
}
