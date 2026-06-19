package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.port.outgoing.CartPort;
import com.mygitgor.user_service.domain.port.outgoing.OrderPort;
import com.mygitgor.user_service.infrastructure.dto.external.UserDashboardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserDashboardService {
    private final CartPort cartPort;
    private final OrderPort orderPort;

    public Mono<UserDashboardDto> getUserDashboard(String userId) {
        return Mono.zip(
                        cartPort.getCartItemsCount(userId),
                        orderPort.getOrderStatistics(userId)
                )
                .map(tuple -> UserDashboardDto.builder()
                        .cartItemsCount(tuple.getT1())
                        .totalOrders(tuple.getT2().getTotalOrders())
                        .build());
    }
}
