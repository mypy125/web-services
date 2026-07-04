package com.mygitgor.seller_service.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Coupon response DTO")
public record CouponResponse(

        @Schema(description = "Coupon ID", example = "coup-123e4567-e89b-12d3-a456-426614174000")
        String id,

        @Schema(description = "Coupon code", example = "SUMMER2024")
        String code,

        @Schema(description = "Coupon description", example = "Summer sale discount")
        String description,

        @Schema(description = "Coupon type", example = "SELLER", allowableValues = {"GLOBAL", "SELLER", "CATEGORY", "PRODUCT", "FIRST_ORDER", "LOYALTY"})
        String type,

        @Schema(description = "Seller ID", example = "sel-123e4567-e89b-12d3-a456-426614174000")
        String sellerId,

        @Schema(description = "Seller name", example = "Tech Store")
        String sellerName,

        @Schema(description = "Discount type", example = "PERCENTAGE")
        String discountType,

        @Schema(description = "Discount value", example = "15.0")
        Double discountValue,

        @Schema(description = "Discount display", example = "15% OFF")
        String discountDisplay,

        @Schema(description = "Minimum order amount", example = "100.0")
        Double minOrderAmount,

        @Schema(description = "Maximum discount amount", example = "50.0")
        Double maxDiscountAmount,

        @Schema(description = "Valid from", example = "2024-01-01T00:00:00")
        LocalDateTime validFrom,

        @Schema(description = "Valid until", example = "2024-12-31T23:59:59")
        LocalDateTime validUntil,

        @Schema(description = "Is valid", example = "true")
        boolean isValid,

        @Schema(description = "Days until expiration", example = "15")
        Long daysUntilExpiration,

        @Schema(description = "Is expired", example = "false")
        boolean isExpired,

        @Schema(description = "Is active", example = "true")
        boolean isActive,

        @Schema(description = "Usage limit", example = "100")
        Integer usageLimit,

        @Schema(description = "Usage count", example = "45")
        Integer usageCount,

        @Schema(description = "Remaining uses", example = "55")
        Integer remainingUses,

        @Schema(description = "Usage limit per user", example = "1")
        Integer usageLimitPerUser,

        @Schema(description = "User usage count", example = "1")
        Integer userUsageCount,

        @Schema(description = "Is fully used", example = "false")
        boolean isFullyUsed,

        @Schema(description = "Applicable product IDs", example = "[\"prod-123\", \"prod-456\"]")
        List<String> applicableProductIds,

        @Schema(description = "Applicable product names", example = "[\"iPhone 15 Pro\", \"Samsung Galaxy S24\"]")
        List<String> applicableProductNames,

        @Schema(description = "Applicable category IDs", example = "[\"cat-123\", \"cat-456\"]")
        List<String> applicableCategoryIds,

        @Schema(description = "Applicable category names", example = "[\"Electronics\", \"Smartphones\"]")
        List<String> applicableCategoryNames,

        @Schema(description = "Applicable seller IDs", example = "[\"seller-123\"]")
        List<String> applicableSellerIds,

        @Schema(description = "Excluded product IDs", example = "[\"prod-789\"]")
        List<String> excludedProductIds,

        @Schema(description = "Excluded category IDs", example = "[\"cat-789\"]")
        List<String> excludedCategoryIds,

        @Schema(description = "Target user roles", example = "[\"ROLE_CUSTOMER\"]")
        List<String> targetUserRoles,

        @Schema(description = "Is for new users only", example = "true")
        boolean newUsersOnly,

        @Schema(description = "Is for first order only", example = "false")
        boolean firstOrderOnly,

        @Schema(description = "Display name", example = "Summer Sale")
        String displayName,

        @Schema(description = "Display image URL", example = "https://example.com/coupon.png")
        String displayImage,

        @Schema(description = "Terms and conditions", example = "Valid for products over $100")
        String termsAndConditions,

        @Schema(description = "Priority", example = "1")
        Integer priority,

        @Schema(description = "Is visible", example = "true")
        boolean isVisible,

        @Schema(description = "Status", example = "ACTIVE")
        String status,

        @Schema(description = "Created at")
        LocalDateTime createdAt,

        @Schema(description = "Created by", example = "admin@example.com")
        String createdBy,

        @Schema(description = "Updated at")
        LocalDateTime updatedAt,

        @Schema(description = "Updated by", example = "admin@example.com")
        String updatedBy,

        @Schema(description = "Total orders using coupon", example = "45")
        Integer totalOrders,

        @Schema(description = "Total discount given", example = "675.00")
        Double totalDiscountGiven,

        @Schema(description = "Total revenue from orders using coupon", example = "4500.00")
        Double totalRevenue,

        @Schema(description = "Average discount per order", example = "15.00")
        Double averageDiscountPerOrder,

        @Schema(description = "Conversion rate", example = "15.5")
        Double conversionRate
) {

    public boolean isFullyUsed() {
        return usageLimit != null && usageCount != null && usageCount >= usageLimit;
    }

    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(LocalDateTime.now());
    }

    public boolean isUsable() {
        return isActive && isValid && !isExpired() && !isFullyUsed();
    }

    @Override
    public String discountDisplay() {
        if (discountDisplay != null) return discountDisplay;
        if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
            return String.format("%.0f%% OFF", discountValue);
        } else if ("FIXED".equalsIgnoreCase(discountType)) {
            return String.format("$%.2f OFF", discountValue);
        }
        return "";
    }

    @Override
    public Long daysUntilExpiration() {
        if (daysUntilExpiration != null) return daysUntilExpiration;
        if (validUntil == null) return 0L;
        return java.time.Duration.between(LocalDateTime.now(), validUntil).toDays();
    }

    @Override
    public Integer remainingUses() {
        if (remainingUses != null) return remainingUses;
        if (usageLimit == null) return null;
        if (usageCount == null) return usageLimit;
        return Math.max(0, usageLimit - usageCount);
    }
}