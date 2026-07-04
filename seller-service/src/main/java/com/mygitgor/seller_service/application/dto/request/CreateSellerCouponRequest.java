package com.mygitgor.seller_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "Create seller coupon request")
public record CreateSellerCouponRequest(

        @NotBlank(message = "Coupon code is required")
        @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Coupon code must contain only uppercase letters, numbers, underscores and hyphens")
        @Schema(description = "Coupon code", example = "SUMMER2024", required = true)
        String code,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        @Schema(description = "Coupon description", example = "Summer sale discount")
        String description,

        @NotNull(message = "Discount type is required")
        @Schema(description = "Discount type", example = "PERCENTAGE", allowableValues = {"PERCENTAGE", "FIXED"})
        String discountType,

        @NotNull(message = "Discount value is required")
        @Positive(message = "Discount value must be positive")
        @Schema(description = "Discount value", example = "15.0", required = true)
        Double discountValue,

        @Schema(description = "Minimum order amount", example = "100.0")
        @PositiveOrZero(message = "Minimum order amount must be positive or zero")
        Double minOrderAmount,

        @Schema(description = "Maximum discount amount", example = "50.0")
        @Positive(message = "Maximum discount amount must be positive")
        Double maxDiscountAmount,

        @NotNull(message = "Valid from date is required")
        @FutureOrPresent(message = "Valid from date must be present or future")
        @Schema(description = "Valid from date", example = "2024-01-01T00:00:00", required = true)
        LocalDateTime validFrom,

        @NotNull(message = "Valid until date is required")
        @Future(message = "Valid until date must be in future")
        @Schema(description = "Valid until date", example = "2024-12-31T23:59:59", required = true)
        LocalDateTime validUntil,

        @Schema(description = "Usage limit", example = "100")
        @Positive(message = "Usage limit must be positive")
        Integer usageLimit,

        @Schema(description = "Usage limit per user", example = "1")
        @Positive(message = "Usage limit per user must be positive")
        Integer usageLimitPerUser,

        @Schema(description = "Applicable product IDs", example = "[\"prod-123\", \"prod-456\"]")
        List<String> applicableProductIds,

        @Schema(description = "Applicable category IDs", example = "[\"cat-123\", \"cat-456\"]")
        List<String> applicableCategoryIds,

        @Schema(description = "Applicable seller IDs (for multi-seller coupons)", example = "[\"seller-123\"]")
        List<String> applicableSellerIds,

        @Schema(description = "Excluded product IDs", example = "[\"prod-789\"]")
        List<String> excludedProductIds,

        @Schema(description = "Excluded category IDs", example = "[\"cat-789\"]")
        List<String> excludedCategoryIds,

        @Schema(description = "Target user roles", example = "[\"ROLE_CUSTOMER\", \"ROLE_SELLER\"]")
        List<String> targetUserRoles,

        @Schema(description = "Target user IDs (specific users)", example = "[\"user-123\", \"user-456\"]")
        List<String> targetUserIds,

        @Schema(description = "Is for new users only", example = "true")
        boolean newUsersOnly,

        @Schema(description = "Is for first order only", example = "false")
        boolean firstOrderOnly,

        @Schema(description = "Is active", example = "true")
        boolean isActive,

        @Schema(description = "Is visible on storefront", example = "true")
        boolean isVisible,

        @Schema(description = "Display name", example = "Summer Sale")
        String displayName,

        @Schema(description = "Display image URL", example = "https://example.com/coupon.png")
        String displayImage,

        @Schema(description = "Terms and conditions", example = "Valid for products over $100")
        String termsAndConditions,

        @Schema(description = "Priority", example = "1")
        Integer priority
) {

    public boolean isValidDateRange() {
        if (validFrom == null || validUntil == null) return false;
        return validFrom.isBefore(validUntil) || validFrom.isEqual(validUntil);
    }

    public boolean hasApplicableProducts() {
        return applicableProductIds != null && !applicableProductIds.isEmpty();
    }

    public boolean hasApplicableCategories() {
        return applicableCategoryIds != null && !applicableCategoryIds.isEmpty();
    }

    public boolean hasExcludedProducts() {
        return excludedProductIds != null && !excludedProductIds.isEmpty();
    }

    public boolean hasExcludedCategories() {
        return excludedCategoryIds != null && !excludedCategoryIds.isEmpty();
    }

    public boolean hasTargetUsers() {
        return targetUserIds != null && !targetUserIds.isEmpty();
    }

    public boolean isValidDiscountType() {
        return "PERCENTAGE".equalsIgnoreCase(discountType) || "FIXED".equalsIgnoreCase(discountType);
    }

    public boolean isValidDiscountValue() {
        if (discountValue == null) return false;
        if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
            return discountValue > 0 && discountValue <= 100;
        }
        return discountValue > 0;
    }
}