package com.mygitgor.seller_service.domain.port.outgoing;

import com.mygitgor.seller_service.application.dto.external.OrderDetailsDto;
import com.mygitgor.seller_service.application.dto.external.OrderStatisticsDto;
import com.mygitgor.seller_service.application.dto.external.OrderSummaryDto;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface OrderPort {
    Mono<OrderDetailsDto> getOrderDetails(OrderId orderId);
    Flux<OrderSummaryDto> getOrdersBySellerId(SellerId sellerId, int page, int size);
    Flux<OrderSummaryDto> getOrdersBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );
    Mono<OrderStatisticsDto> getOrderStatistics(SellerId sellerId);
    Mono<OrderStatisticsDto> getOrderStatistics(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Mono<Long> countOrdersBySellerId(SellerId sellerId);
    Mono<Long> countOrdersBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Mono<Boolean> hasOrders(SellerId sellerId);
    Mono<Boolean> hasOrders(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
