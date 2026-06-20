package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.port.outgoing.*;
import com.mygitgor.user_service.domain.repository.UserRepositoryPort;
import com.mygitgor.user_service.infrastructure.dto.external.*;
import com.mygitgor.user_service.infrastructure.mapper.UserMapper;
import com.mygitgor.user_service.infrastructure.shared.exception.UserNotFoundException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserDashboardService {
    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;
    private final AddressPort addressPort;
    private final CartPort cartPort;
    private final OrderPort orderPort;
    private final CouponPort couponPort;
    private final PaymentPort paymentPort;

    private static final int RECENT_ORDERS_LIMIT = 5;

    public Mono<UserDashboardDto> getUserDashboard(UserId userId) {
        log.debug("Building dashboard for user: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId.getValue().toString())))
                .flatMap(user -> {
                    String userIdStr = userId.toString();

                    return Mono.zip(
                            Mono.just(userMapper.toProfileDto(user)),
                            addressPort.getDefaultAddress(userIdStr)
                                    .onErrorResume(e -> {
                                        log.warn("Failed to get default address for user {}: {}", userId, e.getMessage());
                                        return Mono.empty();
                                    }),
                            cartPort.getUserCartSummary(userIdStr)
                                    .onErrorResume(e -> {
                                        log.warn("Failed to get cart for user {}: {}", userId, e.getMessage());
                                        return Mono.empty();
                                    }),
                            orderPort.getOrderStatistics(userIdStr)
                                    .onErrorResume(e -> {
                                        log.warn("Failed to get order statistics for user {}: {}", userId, e.getMessage());
                                        return Mono.empty();
                                    }),
                            orderPort.getUserOrders(userIdStr, 0, RECENT_ORDERS_LIMIT)
                                    .collectList()
                                    .onErrorResume(e -> {
                                        log.warn("Failed to get recent orders for user {}: {}", userId, e.getMessage());
                                        return Mono.just(Collections.emptyList());
                                    }),
                            couponPort.getUserCoupons(userIdStr)
                                    .count()
                                    .onErrorResume(e -> {
                                        log.warn("Failed to get coupons count for user {}: {}", userId, e.getMessage());
                                        return Mono.just(0L);
                                    })
                    );
                })
                .map(tuple -> {
                    UserProfileDto profile = tuple.getT1();
                    AddressDto defaultAddress = tuple.getT2();
                    CartSummaryDto cart = tuple.getT3();
                    OrderStatisticsDto orderStats = tuple.getT4();
                    List<OrderSummaryDto> recentOrders = tuple.getT5();
                    Long couponsCount = tuple.getT6();

                    return UserDashboardDto.builder()
                            .profile(profile)
                            .defaultAddress(defaultAddress)
                            .cartItemsCount(cart.totalItems())
                            .cartTotal(cart.total())
                            .cartSummary(cart)
                            .totalOrders(orderStats.totalOrders())
                            .totalSpent(orderStats.totalSpent())
                            .averageOrderValue(orderStats.averageOrderValue())
                            .lastOrderDate(orderStats.lastOrderDate())
                            .recentOrders(recentOrders)
                            .availableCouponsCount(couponsCount.intValue())
                            .orderStatusCounts(orderStats.orderStatusCounts())
                            .lastActiveAt(profile.lastLoginAt())
                            .memberSince(profile.createdAt())
                            .build();
                })
                .doOnSuccess(dashboard -> log.debug("Dashboard built successfully for user: {}", userId))
                .doOnError(error -> log.error("Failed to build dashboard for user {}: {}", userId, error.getMessage()));
    }

    public Mono<UserDashboardSummaryDto> getDashboardSummary(UserId userId) {
        log.debug("Getting dashboard summary for user: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId.getValue().toString())))
                .flatMap(user -> {
                    String userIdStr = userId.toString();

                    return Mono.zip(
                            cartPort.getCartItemsCount(userIdStr)
                                    .onErrorResume(e -> {
                                        log.warn("Failed to get cart items count: {}", e.getMessage());
                                        return Mono.just(0);
                                    }),
                            orderPort.getOrderStatistics(userIdStr)
                                    .onErrorResume(e -> {
                                        log.warn("Failed to get order statistics: {}", e.getMessage());
                                        return Mono.just(OrderStatisticsDto.empty());
                                    }),
                            couponPort.getUserCoupons(userIdStr)
                                    .count()
                                    .onErrorResume(e -> {
                                        log.warn("Failed to get coupons count: {}", e.getMessage());
                                        return Mono.just(0L);
                                    })
                    );
                })
                .map(tuple -> {
                    int cartItems = tuple.getT1();
                    OrderStatisticsDto orderStats = tuple.getT2();
                    Long couponsCount = tuple.getT3();

                    return UserDashboardSummaryDto.builder()
                            .cartItemsCount(cartItems)
                            .totalOrders(orderStats.totalOrders())
                            .totalSpent(orderStats.totalSpent())
                            .availableCouponsCount(couponsCount.intValue())
                            .pendingOrders(orderStats.pendingOrders())
                            .processingOrders(orderStats.processingOrders())
                            .shippedOrders(orderStats.shippedOrders())
                            .deliveredOrders(orderStats.deliveredOrders())
                            .lastOrderDate(orderStats.lastOrderDate())
                            .build();
                })
                .doOnSuccess(summary -> log.debug("Dashboard summary built for user: {}", userId));
    }

    public Mono<OrderStatisticsDto> getOrderStatistics(UserId userId) {
        log.debug("Getting order statistics for user: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId.getValue().toString())))
                .flatMap(user -> {
                    String userIdStr = userId.toString();
                    return orderPort.getOrderStatistics(userIdStr)
                            .onErrorResume(e -> {
                                log.warn("Failed to get order statistics for user {}: {}", userId, e.getMessage());
                                return Mono.just(OrderStatisticsDto.empty());
                            });
                })
                .doOnSuccess(stats -> log.debug("Order statistics retrieved for user: {}", userId))
                .doOnError(error -> log.error("Failed to get order statistics for user {}: {}", userId, error.getMessage()));
    }

    public Mono<OrderStatusStatisticsDto> getOrderStatusStatistics(UserId userId) {
        log.debug("Getting order status statistics for user: {}", userId);

        return getOrderStatistics(userId)
                .map(stats -> OrderStatusStatisticsDto.builder()
                        .userId(userId.toString())
                        .statusCounts(stats.orderStatusCounts())
                        .pendingOrders(stats.pendingOrders())
                        .processingOrders(stats.processingOrders())
                        .shippedOrders(stats.shippedOrders())
                        .deliveredOrders(stats.deliveredOrders())
                        .cancelledOrders(stats.cancelledOrders())
                        .refundedOrders(stats.refundedOrders())
                        .activeOrders(stats.getActiveOrdersCount())
                        .completedOrders(stats.getCompletedOrdersCount())
                        .completionRate(stats.getCompletionRate())
                        .build()
                );
    }
}
