package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Integration event emitted when a user successfully redeems a discount coupon or promo code")
public record CouponStatsUpdatedEvent(

        @Schema(description = "Unique identifier of the user who applied the coupon", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The updated total count of coupons used by this user lifetime", example = "5")
        @NotNull(message = "Coupons used count cannot be null")
        @PositiveOrZero(message = "Coupons used must be zero or a positive number")
        Integer couponsUsed,

        @Schema(description = "The updated gross monetary value saved by the user via discounts", example = "145.20")
        @NotNull(message = "Total discount received cannot be null")
        @PositiveOrZero(message = "Total discount received must be zero or a positive number")
        Double totalDiscountReceived,

        @Schema(description = "Alphanumeric code of the coupon used in this transaction", example = "SUMMER2026")
        @NotBlank(message = "Last used coupon code cannot be blank")
        String lastUsedCouponCode,

        @Schema(description = "The specific discount value applied by this individual coupon", example = "15.00")
        @NotNull(message = "Last used discount amount cannot be null")
        @Positive(message = "Last used discount amount must be a positive number")
        Double lastUsedDiscountAmount,

        @Schema(description = "The exact timestamp when the coupon activation event occurred", example = "2026-06-16T18:15:22")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}
