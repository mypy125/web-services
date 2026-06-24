package com.mygitgor.seller_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SellerStatistics {
    // TODO: Counts
    private Long totalSellers;
    private Long activeSellers;
    private Long suspendedSellers;
    private Long bannedSellers;
    private Long pendingVerification;
    private Long fullyVerified;
    private Long rejected;

    // TODO: Averages
    private Double averageRating;
    private Double averageOrderValue;
    private Double averageCommissionRate;
    private Double averageResponseRate;
    private Double averageResponseTimeHours;

    // TODO: Totals
    private Double totalEarnings;
    private Double totalSales;
    private Double totalCommissionPaid;
    private Integer totalOrders;
    private Integer totalProducts;

    // TODO: Timestamps
    private LocalDateTime calculatedAt;
}
