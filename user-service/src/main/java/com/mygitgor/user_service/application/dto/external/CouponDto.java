package com.mygitgor.user_service.application.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

@Schema(description = "Data transfer object representing a discount coupon assigned to a user")
public record CouponDto(

        @Schema(description = "Unique identifier of the coupon record", example = "coup-9941-lmn")
        @NotBlank(message = "Coupon ID cannot be blank")
        String id,

        @Schema(description = "The alphanumeric promo code string", example = "SUMMER2026")
        @NotBlank(message = "Coupon code cannot be blank")
        String code,

        @Schema(description = "Public description explaining the coupon terms", example = "Get 15% off on electronics with a minimum order of $50")
        String description,

        @Schema(description = "Type of the discount strategy applied", example = "PERCENTAGE")
        @NotBlank(message = "Discount type cannot be blank")
        String discountType,

        @Schema(description = "The nominal value of the discount (percentage or fixed amount)", example = "15.0")
        @NotNull(message = "Discount value cannot be null")
        @Positive(message = "Discount value must be a positive number")
        Double discountValue,

        @Schema(description = "Minimum order subtotal required to activate the coupon", example = "50.0")
        @PositiveOrZero(message = "Minimum order amount must be zero or positive")
        Double minOrderAmount,

        @Schema(description = "Maximum monetary cap for percentage-based discounts", example = "25.0")
        @PositiveOrZero(message = "Maximum discount amount must be zero or positive")
        Double maxDiscountAmount,

        @Schema(description = "The starting boundary of coupon validity", example = "2026-06-01T00:00:00")
        @NotNull(message = "Valid from timestamp cannot be null")
        LocalDateTime validFrom,

        @Schema(description = "The expiration boundary of coupon validity", example = "2026-08-31T23:59:59")
        @NotNull(message = "Valid until timestamp cannot be null")
        LocalDateTime validUntil,

        @Schema(description = "Flag indicating if this individual coupon has already been redeemed", example = "false")
        boolean used,

        @Schema(description = "The exact timestamp when this coupon was applied and locked", example = "2026-06-15T14:22:10")
        LocalDateTime usedAt
) {
    public boolean isExpired(LocalDateTime now) {
        return now.isBefore(validFrom) || now.isAfter(validUntil);
    }
}
