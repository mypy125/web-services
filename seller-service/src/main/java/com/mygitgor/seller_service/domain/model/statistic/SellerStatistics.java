package com.mygitgor.seller_service.domain.model.statistic;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SellerStatistics {
    private Long totalSellers;
    private Long activeSellers;
    private Long suspendedSellers;
    private Long bannedSellers;
    private Long pendingVerification;
    private Long fullyVerified;
    private Long rejected;

    private Double averageRating;
    private Double averageOrderValue;
    private Double averageCommissionRate;
    private Double averageResponseRate;
    private Double averageResponseTimeHours;

    private Double totalEarnings;
    private Double totalSales;
    private Double totalCommissionPaid;
    private Integer totalOrders;
    private Integer totalProducts;

    private LocalDateTime calculatedAt;

    public static SellerStatistics empty() {
        return SellerStatistics.builder()
                .totalSellers(0L)
                .activeSellers(0L)
                .suspendedSellers(0L)
                .bannedSellers(0L)
                .pendingVerification(0L)
                .fullyVerified(0L)
                .rejected(0L)
                .averageRating(0.0)
                .averageOrderValue(0.0)
                .averageCommissionRate(0.0)
                .averageResponseRate(0.0)
                .averageResponseTimeHours(0.0)
                .totalEarnings(0.0)
                .totalSales(0.0)
                .totalCommissionPaid(0.0)
                .totalOrders(0)
                .totalProducts(0)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    public Double getActiveSellersPercentage() {
        if (totalSellers == 0) return 0.0;
        return (activeSellers.doubleValue() / totalSellers.doubleValue()) * 100.0;
    }

    public Double getAverageEarningsPerActiveSeller() {
        if (activeSellers == 0) return 0.0;
        return totalEarnings / activeSellers.doubleValue();
    }

    public Double getPlatformCommissionSharePercentage() {
        if (totalSales == 0.0) return 0.0;
        return (totalCommissionPaid / totalSales) * 100.0;
    }

    public boolean isEmpty() {
        return totalSellers == 0 && totalOrders == 0 && totalSales == 0.0;
    }
}
