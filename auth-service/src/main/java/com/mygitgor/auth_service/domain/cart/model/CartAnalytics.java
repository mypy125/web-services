package com.mygitgor.auth_service.domain.cart.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class CartAnalytics {
    private final long totalActiveCarts;
    private final long totalAbandonedCarts;
    private final double averageCartValue;
    private final double conversionRate;
    private final Map<String, Long> topProductsInCarts;
    private final Map<LocalDateTime, Long> cartsByHour;
    private final double averageItemsPerCart;
    private final long cartsWithCoupons;
}
