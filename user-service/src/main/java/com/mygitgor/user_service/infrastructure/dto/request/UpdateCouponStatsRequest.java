package com.mygitgor.user_service.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Request package to update user coupon and discount statistics")
public record UpdateCouponStatsRequest(

        @Schema(description = "The specific coupon code that was applied", example = "SUMMER2026")
        String couponCode,

        @Schema(description = "The monetary value of the discount received", example = "15.50")
        @NotNull(message = "Discount amount cannot be null")
        @PositiveOrZero(message = "Discount amount must be zero or a positive number")
        Double discountAmount
) {}
