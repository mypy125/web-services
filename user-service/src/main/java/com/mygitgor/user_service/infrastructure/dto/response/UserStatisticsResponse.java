package com.mygitgor.user_service.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Comprehensive analytical response holding aggregated user activity, financial, and loyalty statistics")
public record UserStatisticsResponse(

        @Schema(description = "Unique identifier of the user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "User's electronic mail address", example = "alex.patterson@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Total number of completed orders", example = "42")
        @NotNull(message = "Total orders count cannot be null")
        @PositiveOrZero(message = "Total orders must be zero or a positive number")
        Integer totalOrders,

        @Schema(description = "Lifetime gross monetary value spent by the user", example = "3540.50")
        @NotNull(message = "Total spent cannot be null")
        @PositiveOrZero(message = "Total spent must be zero or a positive number")
        Double totalSpent,

        @Schema(description = "Calculated mathematical mean of user check size", example = "84.30")
        @NotNull(message = "Average order value cannot be null")
        @PositiveOrZero(message = "Average order value must be zero or a positive number")
        Double averageOrderValue,

        @Schema(description = "Timestamp of the most recently placed order", example = "2026-06-15T14:22:10")
        LocalDateTime lastOrderDate,

        @Schema(description = "Total count of physical item units purchased across all orders", example = "114")
        @NotNull(message = "Total products purchased cannot be null")
        @PositiveOrZero(message = "Total products purchased must be zero or positive")
        Integer totalProductsPurchased,

        @Schema(description = "The commercial category name from which the user buys most frequently", example = "Electronics")
        String mostPurchasedCategory,

        @Schema(description = "Identifier of the most frequently ordered product", example = "prod-9921-xyz")
        String favoriteProductId,

        @Schema(description = "Display name of the most frequently ordered product", example = "Mechanical Keyboard")
        String favoriteProductName,

        @Schema(description = "Total number of written product reviews", example = "15")
        @NotNull(message = "Total reviews cannot be null")
        @PositiveOrZero(message = "Total reviews must be zero or positive")
        Integer totalReviews,

        @Schema(description = "Average rating score given by the user across all reviews", example = "4.8")
        @NotNull(message = "Average rating cannot be null")
        @Min(value = 0, message = "Average rating cannot be less than 0")
        @Max(value = 5, message = "Average rating cannot exceed 5")
        Double averageRating,

        @Schema(description = "Timestamp of the user's last interaction with the platform", example = "2026-06-15T18:00:00")
        @NotNull(message = "Last active timestamp cannot be null")
        LocalDateTime lastActiveAt,

        @Schema(description = "Total lifetime days the user has interacted with the platform", example = "120")
        @NotNull(message = "Days active cannot be null")
        @PositiveOrZero(message = "Days active must be zero or positive")
        Integer daysActive,

        @Schema(description = "Current streak of consecutive calendar days with successful logins", example = "7")
        @NotNull(message = "Consecutive login days cannot be null")
        @PositiveOrZero(message = "Consecutive login days must be zero or positive")
        Integer consecutiveLoginDays,

        @Schema(description = "Current number of items saved in the wishlist", example = "23")
        @NotNull(message = "Wishlist items count cannot be null")
        @PositiveOrZero(message = "Wishlist items count must be zero or positive")
        Integer wishlistItemsCount,

        @Schema(description = "Current number of active items inside the shopping cart", example = "3")
        @NotNull(message = "Cart items count cannot be null")
        @PositiveOrZero(message = "Cart items count must be zero or positive")
        Integer cartItemsCount,

        @Schema(description = "Total number of discount coupons successfully redeemed", example = "5")
        @NotNull(message = "Coupons used cannot be null")
        @PositiveOrZero(message = "Coupons used must be zero or positive")
        Integer couponsUsed,

        @Schema(description = "Total monetary value saved by utilizing coupons and promo codes", example = "120.45")
        @NotNull(message = "Total discount received cannot be null")
        @PositiveOrZero(message = "Total discount received must be zero or positive")
        Double totalDiscountReceived,

        @Schema(description = "Total accumulated loyalty points currently available", example = "450")
        @NotNull(message = "Loyalty points cannot be null")
        @PositiveOrZero(message = "Loyalty points must be zero or positive")
        Integer loyaltyPoints,

        @Schema(description = "The name of the current loyalty tier", example = "GOLD")
        @NotBlank(message = "Loyalty tier cannot be blank")
        String loyaltyTier,

        @Schema(description = "Numeric representation of the current gamification level", example = "3")
        @NotNull(message = "Current level cannot be null")
        @Min(value = 1, message = "Current level must be at least 1")
        Integer currentLevel,

        @Schema(description = "Total milestones points required to unlock the next level", example = "500")
        @NotNull(message = "Next level points cannot be null")
        @Min(value = 1, message = "Next level points must be at least 1")
        Integer nextLevelPoints,

        @Schema(description = "Remaining points needed to cross into the next level", example = "50")
        @NotNull(message = "Points to next level cannot be null")
        @PositiveOrZero(message = "Points to next level must be zero or positive")
        Integer pointsToNextLevel,

        @Schema(description = "Percentage progress towards the next level milestone (0.0 to 100.0)", example = "90.0")
        @NotNull(message = "Progress to next level cannot be null")
        @Min(value = 0, message = "Progress cannot be less than 0%")
        @Max(value = 100, message = "Progress cannot exceed 100%")
        Double progressToNextLevel,

        @Schema(description = "Timestamp when the statistics record was initialized", example = "2026-01-15T10:00:00")
        @NotNull(message = "Creation timestamp cannot be null")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of the last metric aggregation update", example = "2026-06-15T18:30:00")
        @NotNull(message = "Update timestamp cannot be null")
        LocalDateTime updatedAt
) {}
