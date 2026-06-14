package com.mygitgor.user_service.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_statistics")
public class UserStatisticsEntity {
    @Id
    private UUID userId;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
