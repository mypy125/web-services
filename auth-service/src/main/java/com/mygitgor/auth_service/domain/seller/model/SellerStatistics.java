package com.mygitgor.auth_service.domain.seller.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class SellerStatistics {
    private final String sellerId;
    private final String sellerName;

    private final long totalOrders;
    private final double totalSales;
    private final double averageOrderValue;
    private final double totalCommission;
    private final double netEarnings;

    private final long totalProducts;
    private final long activeProducts;
    private final long outOfStockProducts;

    private final Map<String, Double> salesByDay;
    private final Map<String, Double> salesByCategory;
    private final Map<Integer, Double> salesByMonth;

    private final double conversionRate;
    private final double cancellationRate;
    private final double returnRate;

    private final LocalDateTime periodStart;
    private final LocalDateTime periodEnd;
    private final LocalDateTime calculatedAt;
}
