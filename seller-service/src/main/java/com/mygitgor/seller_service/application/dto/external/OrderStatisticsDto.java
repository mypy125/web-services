package com.mygitgor.seller_service.application.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Schema(description = "Order statistics DTO represented as an immutable record")
public record OrderStatisticsDto(
        @Schema(description = "Seller ID", example = "sel-123e4567-e89b-12d3-a456-426614174000")
        String sellerId,

        @Schema(description = "Total orders", example = "150")
        Integer totalOrders,

        @Schema(description = "Total amount", example = "15000.00")
        Double totalAmount,

        @Schema(description = "Average order value", example = "100.00")
        Double averageOrderValue,

        @Schema(description = "Total tax", example = "1500.00")
        Double totalTax,

        @Schema(description = "Total shipping cost", example = "500.00")
        Double totalShippingCost,

        @Schema(description = "Total discount", example = "1000.00")
        Double totalDiscount,

        @Schema(description = "Total commission", example = "750.00")
        Double totalCommission,

        @Schema(description = "Net earnings", example = "11250.00")
        Double netEarnings,

        @Schema(description = "Pending orders", example = "10")
        Integer pendingOrders,

        @Schema(description = "Processing orders", example = "15")
        Integer processingOrders,

        @Schema(description = "Shipped orders", example = "20")
        Integer shippedOrders,

        @Schema(description = "Delivered orders", example = "80")
        Integer deliveredOrders,

        @Schema(description = "Cancelled orders", example = "15")
        Integer cancelledOrders,

        @Schema(description = "Refunded orders", example = "10")
        Integer refundedOrders,

        @Schema(description = "Status counts map", example = "{\"DELIVERED\": 80, \"PENDING\": 10}")
        Map<String, Integer> statusCounts,

        @Schema(description = "Orders this month", example = "25")
        Integer ordersThisMonth,

        @Schema(description = "Orders last month", example = "20")
        Integer ordersLastMonth,

        @Schema(description = "Amount this month", example = "2500.00")
        Double amountThisMonth,

        @Schema(description = "Amount last month", example = "2000.00")
        Double amountLastMonth,

        @Schema(description = "Month-over-month growth", example = "25.0")
        Double monthlyGrowth,

        @Schema(description = "Average order value this month", example = "100.00")
        Double averageOrderValueThisMonth,

        @Schema(description = "Average order value last month", example = "95.00")
        Double averageOrderValueLastMonth,

        @Schema(description = "Total customers", example = "85")
        Integer totalCustomers,

        @Schema(description = "New customers this period", example = "20")
        Integer newCustomers,

        @Schema(description = "Returning customers", example = "65")
        Integer returningCustomers,

        @Schema(description = "Customer retention rate", example = "76.5")
        Double customerRetentionRate,

        @Schema(description = "Average customer lifetime value", example = "176.47")
        Double averageCustomerLifetimeValue,

        @Schema(description = "Most purchased category", example = "Electronics")
        String mostPurchasedCategory,

        @Schema(description = "Total unique products", example = "45")
        Integer totalUniqueProducts,

        @Schema(description = "Average items per order", example = "2.5")
        Double averageItemsPerOrder,

        @Schema(description = "Top product ID", example = "prd-123e4567-e89b-12d3-a456-426614174000")
        String topProductId,

        @Schema(description = "Top product name", example = "iPhone 15 Pro")
        String topProductName,

        @Schema(description = "Top product sales", example = "150")
        Integer topProductSales,

        @Schema(description = "Conversion rate", example = "15.5")
        Double conversionRate,

        @Schema(description = "Return rate", example = "6.7")
        Double returnRate,

        @Schema(description = "Cancellation rate", example = "10.0")
        Double cancellationRate,

        @Schema(description = "Refund rate", example = "6.7")
        Double refundRate,

        @Schema(description = "Fulfillment rate", example = "85.0")
        Double fulfillmentRate,

        @Schema(description = "On-time delivery rate", example = "90.0")
        Double onTimeDeliveryRate,

        @Schema(description = "Statistics calculated at")
        LocalDateTime calculatedAt,

        @Schema(description = "Period start")
        LocalDateTime periodStart,

        @Schema(description = "Period end")
        LocalDateTime periodEnd
) {}