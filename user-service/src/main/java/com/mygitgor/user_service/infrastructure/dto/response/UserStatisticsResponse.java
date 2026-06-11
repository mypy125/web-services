package com.mygitgor.user_service.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private String userId;
    private String email;
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
    private Integer currentLevel;
    private Integer nextLevelPoints;
    private Integer pointsToNextLevel;
    private Double progressToNextLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
