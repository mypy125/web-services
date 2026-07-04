package com.mygitgor.seller_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "Create coupon request (admin)")
public record CreateCouponRequest(

        @NotBlank(message = "Coupon code is required")
        @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
        @Schema(description = "Coupon code", example = "PLATFORM2024", required = true)
        String code,

        @Schema(description = "Coupon description", example = "Platform-wide discount")
        String description,

        @NotNull(message = "Coupon type is required")
        @Schema(description = "Coupon type", example = "GLOBAL", allowableValues = {"GLOBAL", "CATEGORY", "PRODUCT"})
        String type,

        @NotNull(message = "Discount type is required")
        @Schema(description = "Discount type", example = "PERCENTAGE")
        String discountType,

        @NotNull(message = "Discount value is required")
        @Positive(message = "Discount value must be positive")
        @Schema(description = "Discount value", example = "10.0")
        Double discountValue,

        @Schema(description = "Minimum order amount", example = "50.0")
        @PositiveOrZero(message = "Minimum order amount must be positive or zero")
        Double minOrderAmount,

        @Schema(description = "Maximum discount amount", example = "25.0")
        @Positive(message = "Maximum discount amount must be positive")
        Double maxDiscountAmount,

        @NotNull(message = "Valid from date is required")
        @FutureOrPresent(message = "Valid from date must be present or future")
        @Schema(description = "Valid from date", example = "2024-01-01T00:00:00")
        LocalDateTime validFrom,

        @NotNull(message = "Valid until date is required")
        @Future(message = "Valid until date must be in future")
        @Schema(description = "Valid until date", example = "2024-12-31T23:59:59")
        LocalDateTime validUntil,

        @Schema(description = "Usage limit", example = "1000")
        Integer usageLimit,

        @Schema(description = "Applicable category IDs (for CATEGORY type)", example = "[\"cat-123\"]")
        List<String> applicableCategoryIds,

        @Schema(description = "Applicable product IDs (for PRODUCT type)", example = "[\"prod-123\"]")
        List<String> applicableProductIds,

        @Schema(description = "Is active", example = "true")
        boolean isActive,

        @Schema(description = "Display name", example = "Platform Summer Sale")
        String displayName,

        @Schema(description = "Terms and conditions", example = "Valid for all products")
        String termsAndConditions,

        @Schema(description = "Priority", example = "1")
        Integer priority
) {}
