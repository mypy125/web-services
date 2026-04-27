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
public class SellerStatisticsDto {
    private String sellerId;
    private String sellerName;
    private long totalOrders;
    private double totalSales;
    private double averageOrderValue;
    private double totalCommission;
    private double netEarnings;
    private long totalProducts;
    private long activeProducts;
    private long outOfStockProducts;
    private Map<String, Double> salesByDay;
    private Map<String, Double> salesByCategory;
    private Map<Integer, Double> salesByMonth;
    private double conversionRate;
    private double cancellationRate;
    private double returnRate;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime calculatedAt;
}
