package com.mygitgor.seller_service.domain.model;

import com.mygitgor.seller_service.shared.valueobject.OrderStats;
import com.mygitgor.seller_service.shared.valueobject.ReportPeriod;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerReportId;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class SellerReport {
    // TODO: Identification
    private final SellerReportId reportId;
    private final SellerId sellerId;

    // TODO: Period
    private ReportPeriod period;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // TODO: Earnings
    private Double totalEarnings;
    private Double totalSales;
    private Double totalRefunds;
    private Double totalTax;
    private Double netEarnings;
    private Double totalCommission;
    private Double totalShippingCost;
    private Double totalDiscountGiven;
    private Double totalCashbackGiven;

    // TODO: Order Statistics
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer canceledOrders;
    private Integer returnedOrders;
    private Integer refundedOrders;
    private Integer pendingOrders;
    private Integer processingOrders;
    private Integer shippedOrders;
    private Integer deliveredOrders;
    private Integer totalTransactions;

    // TODO: Product Statistics
    private Integer totalProductsSold;
    private Integer totalUniqueProductsSold;
    private String bestSellingProductId;
    private String bestSellingProductName;
    private Integer bestSellingProductQuantity;
    private Double bestSellingProductRevenue;
    private String topCategory;
    private Integer topCategorySales;

    // TODO: Customer Statistics
    private Integer totalCustomers;
    private Integer newCustomers;
    private Integer returningCustomers;
    private Double customerRetentionRate;
    private Double averageOrderValue;
    private Double averageCustomerLifetimeValue;

    // TODO: Rating Statistics
    private Double averageRating;
    private Integer totalReviews;
    private Integer positiveReviews;
    private Integer neutralReviews;
    private Integer negativeReviews;
    private Double responseRate;
    private Double averageResponseTimeHours;

    // TODO: Performance Metric
    private Double conversionRate;
    private Double returnRate;
    private Double cancellationRate;
    private Double refundRate;
    private Double fulfillmentRate;
    private Double onTimeDeliveryRate;
    private Double profitMargin;

    // TODO: Comparison
    private Double growthPercentage;
    private Map<String, Double> comparisonMetrics;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reportGeneratedAt;


    public static SellerReport createNew(SellerId sellerId, ReportPeriod period, LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        return SellerReport.builder()
                .reportId(new SellerReportId())
                .sellerId(sellerId)
                .period(period)
                .periodStart(start)
                .periodEnd(end)
                .totalEarnings(0.0)
                .totalSales(0.0)
                .totalRefunds(0.0)
                .totalTax(0.0)
                .netEarnings(0.0)
                .totalCommission(0.0)
                .totalShippingCost(0.0)
                .totalDiscountGiven(0.0)
                .totalCashbackGiven(0.0)
                .totalOrders(0)
                .completedOrders(0)
                .canceledOrders(0)
                .returnedOrders(0)
                .refundedOrders(0)
                .pendingOrders(0)
                .processingOrders(0)
                .shippedOrders(0)
                .deliveredOrders(0)
                .totalTransactions(0)
                .totalProductsSold(0)
                .totalUniqueProductsSold(0)
                .totalCustomers(0)
                .newCustomers(0)
                .returningCustomers(0)
                .customerRetentionRate(0.0)
                .averageOrderValue(0.0)
                .averageCustomerLifetimeValue(0.0)
                .averageRating(0.0)
                .totalReviews(0)
                .positiveReviews(0)
                .neutralReviews(0)
                .negativeReviews(0)
                .responseRate(100.0)
                .averageResponseTimeHours(0.0)
                .conversionRate(0.0)
                .returnRate(0.0)
                .cancellationRate(0.0)
                .refundRate(0.0)
                .fulfillmentRate(0.0)
                .onTimeDeliveryRate(0.0)
                .profitMargin(0.0)
                .growthPercentage(0.0)
                .createdAt(now)
                .updatedAt(now)
                .reportGeneratedAt(now)
                .build();
    }

    public void updateOrderStats(OrderStats orderStats) {
        this.totalOrders = (this.totalOrders == null ? 0 : this.totalOrders) + 1;
        this.totalSales = (this.totalSales == null ? 0.0 : this.totalSales) + orderStats.getAmount();
        this.totalTax = (this.totalTax == null ? 0.0 : this.totalTax) + orderStats.getTax();
        this.totalShippingCost = (this.totalShippingCost == null ? 0.0 : this.totalShippingCost) + orderStats.getShippingCost();
        this.totalDiscountGiven = (this.totalDiscountGiven == null ? 0.0 : this.totalDiscountGiven) + orderStats.getDiscount();
        this.totalCommission = (this.totalCommission == null ? 0.0 : this.totalCommission) + orderStats.getCommission();

        this.totalEarnings = this.totalSales - this.totalRefunds - this.totalCommission - this.totalShippingCost - this.totalTax;
        this.netEarnings = this.totalEarnings - this.totalTax;

        this.averageOrderValue = this.totalOrders > 0 ? this.totalSales / this.totalOrders : 0.0;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateOrderStatus(String status) {
        switch (status.toUpperCase()) {
            case "COMPLETED" -> this.completedOrders = (this.completedOrders == null ? 0 : this.completedOrders) + 1;
            case "CANCELED" -> this.canceledOrders = (this.canceledOrders == null ? 0 : this.canceledOrders) + 1;
            case "RETURNED" -> this.returnedOrders = (this.returnedOrders == null ? 0 : this.returnedOrders) + 1;
            case "REFUNDED" -> this.refundedOrders = (this.refundedOrders == null ? 0 : this.refundedOrders) + 1;
            case "PENDING" -> this.pendingOrders = (this.pendingOrders == null ? 0 : this.pendingOrders) + 1;
            case "PROCESSING" -> this.processingOrders = (this.processingOrders == null ? 0 : this.processingOrders) + 1;
            case "SHIPPED" -> this.shippedOrders = (this.shippedOrders == null ? 0 : this.shippedOrders) + 1;
            case "DELIVERED" -> this.deliveredOrders = (this.deliveredOrders == null ? 0 : this.deliveredOrders) + 1;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRating(Integer rating) {
        int total = this.totalReviews == null ? 0 : this.totalReviews;
        double currentTotal = this.averageRating == null ? 0.0 : this.averageRating * total;

        this.totalReviews = total + 1;
        this.averageRating = (currentTotal + rating) / this.totalReviews;
        this.updatedAt = LocalDateTime.now();

        if (rating >= 4) {
            this.positiveReviews = (this.positiveReviews == null ? 0 : this.positiveReviews) + 1;
        } else if (rating == 3) {
            this.neutralReviews = (this.neutralReviews == null ? 0 : this.neutralReviews) + 1;
        } else {
            this.negativeReviews = (this.negativeReviews == null ? 0 : this.negativeReviews) + 1;
        }
    }

    public void updateCustomerStats(boolean isNewCustomer) {
        if (this.totalCustomers == null) this.totalCustomers = 0;
        this.totalCustomers++;

        if (isNewCustomer) {
            this.newCustomers = (this.newCustomers == null ? 0 : this.newCustomers) + 1;
        } else {
            this.returningCustomers = (this.returningCustomers == null ? 0 : this.returningCustomers) + 1;
        }

        if (this.totalCustomers > 0) {
            this.customerRetentionRate = (double) (this.returningCustomers != null ? this.returningCustomers : 0) / this.totalCustomers * 100;
            this.averageCustomerLifetimeValue = this.totalSales / this.totalCustomers;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void updateProductStats(String productId, String productName, String category, Integer quantity, Double revenue) {
        this.totalProductsSold = (this.totalProductsSold == null ? 0 : this.totalProductsSold) + quantity;

        if (this.totalUniqueProductsSold == null) this.totalUniqueProductsSold = 0;
        this.totalUniqueProductsSold++;

        if (this.bestSellingProductRevenue == null || revenue > this.bestSellingProductRevenue) {
            this.bestSellingProductId = productId;
            this.bestSellingProductName = productName;
            this.bestSellingProductQuantity = quantity;
            this.bestSellingProductRevenue = revenue;
        }

        if (this.topCategorySales == null || this.topCategorySales < quantity) {
            this.topCategory = category;
            this.topCategorySales = quantity;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void calculateDerivedMetrics() {
        if (this.totalOrders != null && this.totalOrders > 0) {
            this.returnRate = (double) (this.returnedOrders != null ? this.returnedOrders : 0) / this.totalOrders * 100;
            this.cancellationRate = (double) (this.canceledOrders != null ? this.canceledOrders : 0) / this.totalOrders * 100;
            this.refundRate = (double) (this.refundedOrders != null ? this.refundedOrders : 0) / this.totalOrders * 100;
            this.fulfillmentRate = (double) (this.completedOrders != null ? this.completedOrders : 0) / this.totalOrders * 100;

            if (this.totalSales != null && this.totalSales > 0) {
                this.profitMargin = (this.netEarnings != null ? this.netEarnings : 0.0) / this.totalSales * 100;
            }
        }

        if (this.totalReviews != null && this.totalReviews > 0) {
            this.positiveReviews = this.positiveReviews != null ? this.positiveReviews : 0;
            this.neutralReviews = this.neutralReviews != null ? this.neutralReviews : 0;
            this.negativeReviews = this.negativeReviews != null ? this.negativeReviews : 0;
        }

        this.updatedAt = LocalDateTime.now();
        this.reportGeneratedAt = LocalDateTime.now();
    }

    public void setComparisonMetrics(Map<String, Double> comparisonMetrics, Double growthPercentage) {
        this.comparisonMetrics = comparisonMetrics;
        this.growthPercentage = growthPercentage;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasData() {
        return (this.totalOrders != null && this.totalOrders > 0) ||
                (this.totalSales != null && this.totalSales > 0);
    }
}