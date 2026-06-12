package com.mygitgor.user_service.infrastructure.mapper;

import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.infrastructure.dto.response.LoyaltyInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserStatisticsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserStatisticsMapper {

    @Mapping(target = "userId", source = "userId.value")
    UserStatisticsResponse toResponse(UserStatistics stats);

    @Mapping(target = "userId", source = "userId.value")
    @Mapping(target = "currentLevel", expression = "java(getLevel(stats.getLoyaltyPoints()))")
    @Mapping(target = "nextLevelPoints", expression = "java(calculateNextLevelPoints(stats.getLoyaltyPoints()))")
    @Mapping(target = "pointsToNextLevel", expression = "java(calculatePointsToNextLevel(stats.getLoyaltyPoints()))")
    @Mapping(target = "progressToNextLevel", expression = "java(calculateProgressToNextLevel(stats.getLoyaltyPoints()))")
    @Mapping(target = "cashbackRate", expression = "java(getCashbackRate(stats.getLoyaltyTier()))")
    @Mapping(target = "discountRate", expression = "java(getDiscountRate(stats.getLoyaltyTier()))")
    @Mapping(target = "benefits", expression = "java(getBenefits(stats.getLoyaltyTier()))")
    LoyaltyInfoResponse toLoyaltyInfo(UserStatistics stats);

    @Named("calculateNextLevelPoints")
    default int calculateNextLevelPoints(int points) {
        if (points < 1000) return 1000;
        if (points < 5000) return 5000;
        if (points < 10000) return 10000;
        return points;
    }

    @Named("calculatePointsToNextLevel")
    default int calculatePointsToNextLevel(int points) {
        return Math.max(0, calculateNextLevelPoints(points) - points);
    }

    @Named("calculateProgressToNextLevel")
    default double calculateProgressToNextLevel(int points) {
        int nextLevel = calculateNextLevelPoints(points);
        return nextLevel > 0 ? (double) points / nextLevel * 100 : 0;
    }

    @Named("getLevel")
    default int getLevel(int points) {
        if (points < 1000) return 1;
        if (points < 5000) return 2;
        if (points < 10000) return 3;
        return 4;
    }

    @Named("getCashbackRate")
    default double getCashbackRate(String tier) {
        return switch (tier) {
            case "BRONZE" -> 0.5;
            case "SILVER" -> 1.0;
            case "GOLD" -> 2.0;
            case "PLATINUM" -> 3.0;
            default -> 0.0;
        };
    }

    @Named("getDiscountRate")
    default double getDiscountRate(String tier) {
        return switch (tier) {
            case "BRONZE" -> 0.0;
            case "SILVER" -> 2.0;
            case "GOLD" -> 5.0;
            case "PLATINUM" -> 10.0;
            default -> 0.0;
        };
    }

    @Named("getBenefits")
    default String getBenefits(String tier) {
        return switch (tier) {
            case "BRONZE" -> "Welcome bonus, Birthday discount";
            case "SILVER" -> "2% discount, Free delivery on orders above $50";
            case "GOLD" -> "5% discount, Free delivery, Priority support";
            case "PLATINUM" -> "10% discount, Free delivery, VIP support, Early access to sales";
            default -> "";
        };
    }
}
