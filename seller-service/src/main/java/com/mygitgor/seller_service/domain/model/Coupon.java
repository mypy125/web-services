package com.mygitgor.seller_service.domain.model;

import com.mygitgor.seller_service.shared.exception.DomainException;
import com.mygitgor.seller_service.shared.valueobject.id.CouponId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class Coupon {
    private final CouponId id;
    private final String code;
    private final CouponType type;
    private final SellerId sellerId;
    private final DiscountType discountType;
    private final Double discountValue;
    private final Double minOrderAmount;
    private final Double maxDiscountAmount;
    private final LocalDateTime validFrom;
    private final LocalDateTime validUntil;
    private final Integer usageLimit;
    private final Integer usageCount;
    private final boolean isActive;
    private final boolean applicableToAll;
    private final List<String> applicableProductIds;
    private final List<String> applicableCategoryIds;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public enum CouponType {
        GLOBAL, SELLER, CATEGORY, PRODUCT, FIRST_ORDER, LOYALTY
    }

    public enum DiscountType {
        PERCENTAGE, FIXED
    }

    public static Coupon createNewSellerCoupon(
            SellerId sellerId,
            String code,
            String discountTypeStr,
            Double discountValue,
            Double minOrderAmount,
            Double maxDiscountAmount,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            Integer usageLimit,
            List<String> applicableProductIds,
            List<String> applicableCategoryIds
    ) {
        DiscountType parsedDiscountType = DiscountType.valueOf(discountTypeStr.toUpperCase());

        Coupon coupon = Coupon.builder()
                .code(code)
                .type(CouponType.SELLER)
                .sellerId(sellerId)
                .discountType(parsedDiscountType)
                .discountValue(discountValue)
                .minOrderAmount(minOrderAmount != null ? minOrderAmount : 0.0)
                .maxDiscountAmount(maxDiscountAmount)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .usageLimit(usageLimit)
                .usageCount(0)
                .isActive(true)
                .applicableToAll(applicableProductIds == null || applicableProductIds.isEmpty())
                .applicableProductIds(applicableProductIds)
                .applicableCategoryIds(applicableCategoryIds)
                .createdBy(sellerId.toString())
                .build();

        coupon.validateBusinessRules();

        return coupon;
    }

    public void validateBusinessRules() {
        if (code == null || code.isBlank()) {
            throw new DomainException("Coupon code cannot be empty");
        }
        if (discountValue == null || discountValue <= 0) {
            throw new DomainException("Discount value must be strictly positive");
        }
        if (discountType == DiscountType.PERCENTAGE && discountValue > 100) {
            throw new DomainException("Percentage discount cannot exceed 100%");
        }
        if (validFrom == null || validUntil == null) {
            throw new DomainException("Coupon validity dates must be specified");
        }
        if (validUntil.isBefore(validFrom) || validUntil.isEqual(validFrom)) {
            throw new DomainException("Expiration date must be strictly after the start date");
        }
        if (type == CouponType.SELLER && sellerId == null) {
            throw new DomainException("Seller coupon must be linked to a valid Seller ID");
        }
    }

    public boolean isValidNow() {
        LocalDateTime now = LocalDateTime.now();
        return isActive
                && validFrom != null && !now.isBefore(validFrom)
                && validUntil != null && now.isBefore(validUntil)
                && (usageLimit == null || usageCount < usageLimit);
    }

    public boolean canApplyToProduct(String productId, SellerId targetSellerId) {
        if (!isValidNow()) return false;

        if (type == CouponType.GLOBAL) return true;

        if (type == CouponType.SELLER) {
            return this.sellerId != null && this.sellerId.equals(targetSellerId);
        }

        if (type == CouponType.PRODUCT) {
            return applicableProductIds != null && applicableProductIds.contains(productId);
        }

        return false;
    }

    public Coupon incrementUsage() {
        if (usageLimit != null && usageCount >= usageLimit) {
            throw new IllegalStateException("Coupon usage limit reached");
        }
        return this.toBuilder()
                .usageCount(this.usageCount + 1)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}