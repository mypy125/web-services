package com.mygitgor.user_service.infrastructure.mapper;

import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.infrastructure.dto.response.LoyaltyInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserStatisticsResponse;
import org.springframework.stereotype.Component;

@Component
public class UserStatisticsMapper {

    public UserStatisticsResponse toResponse(UserStatistics stats) {
        if (stats == null) return null;

        return UserStatisticsResponse.builder()
                .userId(stats.getUserId() != null ? stats.getUserId().toString() : null)
                .totalOrders(stats.getTotalOrders())
                .totalSpent(stats.getTotalSpent())
                .averageOrderValue(stats.getAverageOrderValue())
                .lastOrderDate(stats.getLastOrderDate())
                .totalProductsPurchased(stats.getTotalProductsPurchased())
                .mostPurchasedCategory(stats.getMostPurchasedCategory())
                .favoriteProductId(stats.getFavoriteProductId())
                .favoriteProductName(stats.getFavoriteProductName())
                .totalReviews(stats.getTotalReviews())
                .averageRating(stats.getAverageRating())
                .lastActiveAt(stats.getLastActiveAt())
                .daysActive(stats.getDaysActive())
                .consecutiveLoginDays(stats.getConsecutiveLoginDays())
                .wishlistItemsCount(stats.getWishlistItemsCount())
                .cartItemsCount(stats.getCartItemsCount())
                .couponsUsed(stats.getCouponsUsed())
                .totalDiscountReceived(stats.getTotalDiscountReceived())
                .loyaltyPoints(stats.getLoyaltyPoints())
                .loyaltyTier(stats.getLoyaltyTier())
                .build();
    }

    public LoyaltyInfoResponse toLoyaltyInfo(UserStatistics stats) {
        if (stats == null) return null;

        int nextLevelPoints = calculateNextLevelPoints(stats.getLoyaltyPoints());
        int pointsToNextLevel = Math.max(0, nextLevelPoints - stats.getLoyaltyPoints());
        double progressToNextLevel = nextLevelPoints > 0 ?
                (double) stats.getLoyaltyPoints() / nextLevelPoints * 100 : 0;

        return LoyaltyInfoResponse.builder()
                .userId(stats.getUserId() != null ? stats.getUserId().toString() : null)
                .loyaltyPoints(stats.getLoyaltyPoints())
                .loyaltyTier(stats.getLoyaltyTier())
                .currentLevel(getLevel(stats.getLoyaltyPoints()))
                .nextLevelPoints(nextLevelPoints)
                .pointsToNextLevel(pointsToNextLevel)
                .progressToNextLevel(progressToNextLevel)
                .cashbackRate(getCashbackRate(stats.getLoyaltyTier()))
                .discountRate(getDiscountRate(stats.getLoyaltyTier()))
                .benefits(getBenefits(stats.getLoyaltyTier()))
                .build();
    }

    private int calculateNextLevelPoints(int points) {
        if (points < 1000) return 1000;
        if (points < 5000) return 5000;
        if (points < 10000) return 10000;
        return points;
    }

    private int getLevel(int points) {
        if (points < 1000) return 1;
        if (points < 5000) return 2;
        if (points < 10000) return 3;
        return 4;
    }

    private double getCashbackRate(String tier) {
        return switch (tier) {
            case "BRONZE" -> 0.5;
            case "SILVER" -> 1.0;
            case "GOLD" -> 2.0;
            case "PLATINUM" -> 3.0;
            default -> 0.0;
        };
    }

    private double getDiscountRate(String tier) {
        return switch (tier) {
            case "BRONZE" -> 0.0;
            case "SILVER" -> 2.0;
            case "GOLD" -> 5.0;
            case "PLATINUM" -> 10.0;
            default -> 0.0;
        };
    }

    private String getBenefits(String tier) {
        return switch (tier) {
            case "BRONZE" -> "Welcome bonus, Birthday discount";
            case "SILVER" -> "2% discount, Free delivery on orders above $50";
            case "GOLD" -> "5% discount, Free delivery, Priority support";
            case "PLATINUM" -> "10% discount, Free delivery, VIP support, Early access to sales";
            default -> "";
        };
    }
}
