package com.mygitgor.user_service.infrastructure.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

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
) {}