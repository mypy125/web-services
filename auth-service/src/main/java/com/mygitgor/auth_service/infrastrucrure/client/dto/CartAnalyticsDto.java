package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartAnalyticsDto {
    private long totalActiveCarts;
    private long totalAbandonedCarts;
    private double averageCartValue;
    private double conversionRate;
    private Map<String, Long> topProductsInCarts;
    private Map<LocalDateTime, Long> cartsByHour;
    private double averageItemsPerCart;
    private long cartsWithCoupons;
}
