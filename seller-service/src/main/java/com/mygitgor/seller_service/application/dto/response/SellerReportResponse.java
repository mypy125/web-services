package com.mygitgor.seller_service.application.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record SellerReportResponse(
        String reportId,
        String sellerId,
        String period,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        FinancialStatsResponse earnings,
        OrderStatsResponse orderStats,
        ProductStatsResponse productStats,
        CustomerStatsResponse customerStats,
        RatingStatsResponse ratingStats,
        PerformanceMetricsResponse performanceMetrics,
        Double growthPercentage,
        Map<String, Double> comparisonMetrics,
        LocalDateTime reportGeneratedAt
) {

    @Builder
    public record FinancialStatsResponse(
            Double totalEarnings,
            Double totalSales,
            Double totalRefunds,
            Double totalTax,
            Double netEarnings,
            Double totalCommission,
            Double totalShippingCost,
            Double totalDiscountGiven,
            Double totalCashbackGiven
    ) {}

    @Builder
    public record OrderStatsResponse(
            Integer totalOrders,
            Integer completedOrders,
            Integer canceledOrders,
            Integer returnedOrders,
            Integer refundedOrders,
            Integer pendingOrders,
            Integer processingOrders,
            Integer shippedOrders,
            Integer deliveredOrders,
            Integer totalTransactions
    ) {}

    @Builder
    public record ProductStatsResponse(
            Integer totalProductsSold,
            Integer totalUniqueProductsSold,
            String bestSellingProductId,
            String bestSellingProductName,
            Integer bestSellingProductQuantity,
            Double bestSellingProductRevenue,
            String topCategory,
            Integer topCategorySales
    ) {}

    @Builder
    public record CustomerStatsResponse(
            Integer totalCustomers,
            Integer newCustomers,
            Integer returningCustomers,
            Double customerRetentionRate,
            Double averageOrderValue,
            Double averageCustomerLifetimeValue
    ) {}

    @Builder
    public record RatingStatsResponse(
            Double averageRating,
            Integer totalReviews,
            Integer positiveReviews,
            Integer neutralReviews,
            Integer negativeReviews,
            Double responseRate,
            Double averageResponseTimeHours
    ) {}

    @Builder
    public record PerformanceMetricsResponse(
            Double conversionRate,
            Double returnRate,
            Double cancellationRate,
            Double refundRate,
            Double fulfillmentRate,
            Double onTimeDeliveryRate,
            Double profitMargin
    ) {}
}
