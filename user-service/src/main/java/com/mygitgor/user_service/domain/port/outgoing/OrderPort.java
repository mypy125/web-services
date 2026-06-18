package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.infrastructure.dto.external.OrderStatisticsDto;
import com.mygitgor.user_service.infrastructure.dto.external.OrderSummaryDto;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface OrderPort {
    Flux<OrderSummaryDto> getUserOrders(String userId, int page, int size);
    Mono<OrderStatisticsDto> getOrderStatistics(String userId);
}
