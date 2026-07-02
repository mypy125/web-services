package com.mygitgor.user_service.application.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Schema(description = "Comprehensive analytical data transfer object containing detailed user ordering statistics")
public record OrderStatisticsDto(

        @Schema(description = "Unique identifier of the user", example = "usr-4412-xkg")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "Total lifetime count of orders", example = "42")
        @NotNull @PositiveOrZero Integer totalOrders,

        @Schema(description = "Total lifetime gross spent monetary value", example = "2450.75")
        @NotNull @PositiveOrZero Double totalSpent,

        @Schema(description = "Average calculated gross value per individual order", example = "58.35")
        @NotNull @PositiveOrZero Double averageOrderValue,

        @Schema(description = "The precise timestamp of the last executed order", example = "2026-06-17T12:00:00")
        LocalDateTime lastOrderDate,

        @Schema(description = "Total lifetime quantity of individual products purchased", example = "114")
        @NotNull @PositiveOrZero Integer totalItemsPurchased,

        @Schema(description = "Map of order counts grouped by their statuses", example = "{\"PENDING\": 2, \"DELIVERED\": 35, \"CANCELLED\": 5}")
        @NotNull Map<String, Integer> orderStatusCounts,

        @NotNull @PositiveOrZero Integer pendingOrders,
        @NotNull @PositiveOrZero Integer processingOrders,
        @NotNull @PositiveOrZero Integer shippedOrders,
        @NotNull @PositiveOrZero Integer deliveredOrders,
        @NotNull @PositiveOrZero Integer cancelledOrders,
        @NotNull @PositiveOrZero Integer refundedOrders,

        @NotNull @PositiveOrZero Integer ordersThisMonth,
        @NotNull @PositiveOrZero Integer ordersLastMonth,
        @NotNull @PositiveOrZero Double spentThisMonth,
        @NotNull @PositiveOrZero Double spentLastMonth,

        @Schema(description = "Month-over-month percentage growth or decline rate", example = "14.5")
        @NotNull Double monthlyGrowth,

        @Schema(description = "The name of the shopping category with the most items bought", example = "Electronics")
        String mostPurchasedCategory,

        String favoriteProductId,
        String favoriteProductName,

        @NotNull @PositiveOrZero Integer uniqueProductsPurchased
) {
        public OrderStatisticsDto {
                if (totalOrders == null) totalOrders = 0;
                if (totalSpent == null) totalSpent = 0.0;
                if (averageOrderValue == null) averageOrderValue = 0.0;
                if (totalItemsPurchased == null) totalItemsPurchased = 0;
                if (ordersThisMonth == null) ordersThisMonth = 0;
                if (ordersLastMonth == null) ordersLastMonth = 0;
                if (spentThisMonth == null) spentThisMonth = 0.0;
                if (spentLastMonth == null) spentLastMonth = 0.0;
                if (monthlyGrowth == null) monthlyGrowth = 0.0;
                if (uniqueProductsPurchased == null) uniqueProductsPurchased = 0;

                orderStatusCounts = orderStatusCounts != null ? Map.copyOf(orderStatusCounts) : Map.of();
        }


        public int getPendingCount() {
                return orderStatusCounts.getOrDefault("PENDING", 0);
        }

        public int getDeliveredCount() {
                return orderStatusCounts.getOrDefault("DELIVERED", 0);
        }

        public int getCancelledCount() {
                return orderStatusCounts.getOrDefault("CANCELLED", 0);
        }

        public int getActiveOrdersCount() {
                return (pendingOrders != null ? pendingOrders : 0) +
                        (processingOrders != null ? processingOrders : 0) +
                        (shippedOrders != null ? shippedOrders : 0);
        }

        public int getCompletedOrdersCount() {
                return deliveredOrders != null ? deliveredOrders : 0;
        }

        public double getCompletionRate() {
                if (totalOrders == null || totalOrders == 0) {
                        return 0.0;
                }
                int completed = getCompletedOrdersCount();
                return Math.round(((double) completed / totalOrders) * 100.0) / 100.0;
        }

        public static OrderStatisticsDto empty() {
                return OrderStatisticsDto.builder()
                        .userId("N/A")
                        .totalOrders(0)
                        .totalSpent(0.0)
                        .averageOrderValue(0.0)
                        .lastOrderDate(null)
                        .totalItemsPurchased(0)
                        .orderStatusCounts(java.util.Map.of())
                        .ordersThisMonth(0)
                        .ordersLastMonth(0)
                        .spentThisMonth(0.0)
                        .spentLastMonth(0.0)
                        .monthlyGrowth(0.0)
                        .mostPurchasedCategory("None")
                        .favoriteProductId("N/A")
                        .favoriteProductName("N/A")
                        .uniqueProductsPurchased(0)
                        .build();
        }
}