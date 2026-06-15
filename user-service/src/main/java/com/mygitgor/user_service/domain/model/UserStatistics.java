package com.mygitgor.user_service.domain.model;

import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserStatistics {
    private final UserId userId;
    private Integer totalOrders;
    private Double totalSpent;
    private Double averageOrderValue;
    private LocalDateTime lastOrderDate;
    private Integer totalProductsPurchased;
    private String mostPurchasedCategory;
    private String favoriteProductId;
    private String favoriteProductName;
    private Integer totalReviews;
    private Double averageRating;
    private LocalDateTime lastActiveAt;
    private Integer daysActive;
    private Integer consecutiveLoginDays;
    private Integer wishlistItemsCount;
    private Integer cartItemsCount;
    private Integer couponsUsed;
    private Double totalDiscountReceived;
    private Integer loyaltyPoints;
    private String loyaltyTier;

    public void updateOrderStats(Double orderAmount) {
        if (orderAmount == null || orderAmount <= 0) {
            throw new DomainException("Order amount must be positive");
        }

        if (this.totalOrders == null) this.totalOrders = 0;
        if (this.totalSpent == null) this.totalSpent = 0.0;

        this.totalOrders++;
        this.totalSpent += orderAmount;
        this.averageOrderValue = this.totalSpent / this.totalOrders;
        this.lastOrderDate = LocalDateTime.now();

        int pointsEarned = (int) Math.floor(orderAmount);
        updateLoyaltyPoints(pointsEarned);
    }

    public void updateProductStats(String category, String productId, String productName) {
        if (productId == null) {
            throw new DomainException("Product ID cannot be null");
        }

        if (this.totalProductsPurchased == null) this.totalProductsPurchased = 0;
        this.totalProductsPurchased++;

        if (category != null) {
            this.mostPurchasedCategory = category;
        }
        this.favoriteProductId = productId;
        this.favoriteProductName = productName;
    }

    public void updateReviewStats(Integer rating) {
        if (this.totalReviews == null) this.totalReviews = 0;
        if (this.averageRating == null) this.averageRating = 0.0;

        double totalRating = this.averageRating * this.totalReviews;
        this.totalReviews++;
        this.averageRating = (totalRating + rating) / this.totalReviews;
    }

    public void updateActivity() {
        this.lastActiveAt = LocalDateTime.now();
        if (this.daysActive == null) this.daysActive = 0;
        this.daysActive++;
    }

    public void updateCouponUsage(Double discountAmount) {
        if (this.couponsUsed == null) this.couponsUsed = 0;
        if (this.totalDiscountReceived == null) this.totalDiscountReceived = 0.0;

        this.couponsUsed++;
        this.totalDiscountReceived += discountAmount;
    }

    public void updateLoyaltyPoints(Integer points) {
        if (this.loyaltyPoints == null) this.loyaltyPoints = 0;
        this.loyaltyPoints += points;

        if (this.loyaltyPoints >= 10000) {
            this.loyaltyTier = "PLATINUM";
        } else if (this.loyaltyPoints >= 5000) {
            this.loyaltyTier = "GOLD";
        } else if (this.loyaltyPoints >= 1000) {
            this.loyaltyTier = "SILVER";
        } else {
            this.loyaltyTier = "BRONZE";
        }
    }

    public void updateCartItemsCount(Integer newCount) {
        if (newCount == null || newCount < 0) {
            throw new DomainException("Cart items count cannot be negative");
        }
        this.cartItemsCount = newCount;
    }

    public void updateWishlistItemsCount(Integer newCount) {
        if (newCount == null || newCount < 0) {
            throw new DomainException("Wishlist items count cannot be negative");
        }
        this.wishlistItemsCount = newCount;
    }

    public int getLoyaltyLevel() {
        if (loyaltyPoints == null) return 1;
        if (loyaltyPoints >= 10000) return 4;
        if (loyaltyPoints >= 5000) return 3;
        if (loyaltyPoints >= 1000) return 2;
        return 1;
    }

    public int getPointsToNextLevel() {
        if (loyaltyPoints == null) return 1000;
        if (loyaltyPoints >= 10000) return 0;
        if (loyaltyPoints >= 5000) return 10000 - loyaltyPoints;
        if (loyaltyPoints >= 1000) return 5000 - loyaltyPoints;
        return 1000 - loyaltyPoints;
    }

    public static UserStatistics create(UserId userId) {
        return UserStatistics.builder()
                .userId(userId)
                .totalOrders(0)
                .totalSpent(0.0)
                .averageOrderValue(0.0)
                .totalProductsPurchased(0)
                .totalReviews(0)
                .averageRating(0.0)
                .daysActive(0)
                .couponsUsed(0)
                .totalDiscountReceived(0.0)
                .loyaltyPoints(0)
                .loyaltyTier("BRONZE")
                .wishlistItemsCount(0)
                .cartItemsCount(0)
                .build();
    }
}
