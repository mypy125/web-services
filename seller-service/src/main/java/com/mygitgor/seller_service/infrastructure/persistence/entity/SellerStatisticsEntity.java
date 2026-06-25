package com.mygitgor.seller_service.infrastructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;
import java.time.Duration;

@Table("seller_statistics")
public record SellerStatisticsEntity(
        @Id UUID id,
        @Column("total_sellers") Long totalSellers,
        @Column("active_sellers") Long activeSellers,
        @Column("suspended_sellers") Long suspendedSellers,
        @Column("banned_sellers") Long bannedSellers,
        @Column("pending_verification") Long pendingVerification,
        @Column("fully_verified") Long fullyVerified,
        Long rejected,
        @Column("average_rating") Double averageRating,
        @Column("average_order_value") Double averageOrderValue,
        @Column("average_commission_rate") Double averageCommissionRate,
        @Column("average_response_rate") Double averageResponseRate,
        @Column("average_response_time_hours") Double averageResponseTimeHours,
        @Column("total_earnings") Double totalEarnings,
        @Column("total_sales") Double totalSales,
        @Column("total_commission_paid") Double totalCommissionPaid,
        @Column("total_orders") Integer totalOrders,
        @Column("total_products") Integer totalProducts,
        @Column("calculated_at") LocalDateTime calculatedAt,
        @Column("updated_at") LocalDateTime updatedAt
) {

    public static SellerStatisticsEntity empty() {
        return new SellerStatisticsEntity(
                UUID.randomUUID(), 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0, 0, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    public SellerStatisticsEntity withUpdatedAt(LocalDateTime updatedAt) {
        return new SellerStatisticsEntity(
                id, totalSellers, activeSellers, suspendedSellers, bannedSellers,
                pendingVerification, fullyVerified, rejected, averageRating,
                averageOrderValue, averageCommissionRate, averageResponseRate,
                averageResponseTimeHours, totalEarnings, totalSales, totalCommissionPaid,
                totalOrders, totalProducts, calculatedAt, updatedAt
        );
    }

    public boolean isStale() {
        return calculatedAt == null || Duration.between(calculatedAt, LocalDateTime.now()).toHours() >= 1;
    }

    public boolean isEmpty() {
        return (totalSellers == null || totalSellers == 0) &&
                (totalOrders == null || totalOrders == 0) &&
                (totalProducts == null || totalProducts == 0) &&
                (totalSales == null || totalSales == 0.0);
    }
}