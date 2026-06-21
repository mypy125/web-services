package com.mygitgor.user_service.infrastructure.persistence.repository;

import com.mygitgor.user_service.infrastructure.persistence.entity.UserStatisticsEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface UserStatisticsR2dbcRepository extends ReactiveCrudRepository<UserStatisticsEntity, UUID> {
    Mono<UserStatisticsEntity> findByUserId(UUID userId);
    Flux<UserStatisticsEntity> findByUserIdIn(Collection<UUID> userIds);
    Flux<UserStatisticsEntity> findTop10ByOrderByLoyaltyPointsDesc();
    Flux<UserStatisticsEntity> findByLoyaltyTier(String loyaltyTier);
    @Query("UPDATE user_statistics SET " +
            "total_orders = :totalOrders, " +
            "total_spent = :totalSpent, " +
            "average_order_value = :averageOrderValue, " +
            "last_order_date = :lastOrderDate, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> updateOrderStats(UUID userId, Integer totalOrders, Double totalSpent,
                                Double averageOrderValue, LocalDateTime lastOrderDate);

    @Query("UPDATE user_statistics SET " +
            "total_reviews = :totalReviews, " +
            "average_rating = :averageRating, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> updateReviewStats(UUID userId, Integer totalReviews, Double averageRating);

    @Query("UPDATE user_statistics SET " +
            "last_active_at = :lastActiveAt, " +
            "days_active = :daysActive, " +
            "consecutive_login_days = :consecutiveLoginDays, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> updateActivity(UUID userId, LocalDateTime lastActiveAt,
                              Integer daysActive, Integer consecutiveLoginDays);

    @Query("UPDATE user_statistics SET " +
            "cart_items_count = :cartItemsCount, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> updateCartStats(UUID userId, Integer cartItemsCount);

    @Query("UPDATE user_statistics SET " +
            "wishlist_items_count = :wishlistItemsCount, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> updateWishlistStats(UUID userId, Integer wishlistItemsCount);

    @Query("UPDATE user_statistics SET " +
            "coupons_used = :couponsUsed, " +
            "total_discount_received = :totalDiscountReceived, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> updateCouponStats(UUID userId, Integer couponsUsed, Double totalDiscountReceived);

    @Query("UPDATE user_statistics SET " +
            "loyalty_points = :loyaltyPoints, " +
            "loyalty_tier = :loyaltyTier, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> updateLoyaltyPoints(UUID userId, Integer loyaltyPoints, String loyaltyTier);

    @Query("UPDATE user_statistics SET " +
            "total_products_purchased = :totalProductsPurchased, " +
            "most_purchased_category = :mostPurchasedCategory, " +
            "favorite_product_id = :favoriteProductId, " +
            "favorite_product_name = :favoriteProductName, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> updateProductStats(UUID userId, Integer totalProductsPurchased,
                                  String mostPurchasedCategory, String favoriteProductId,
                                  String favoriteProductName);

    @Query("UPDATE user_statistics SET " +
            "total_orders = total_orders + 1, " +
            "total_spent = total_spent + :amount, " +
            "average_order_value = (total_spent + :amount) / (total_orders + 1), " +
            "last_order_date = :orderDate, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> incrementOrderCount(UUID userId, Double amount, LocalDateTime orderDate);

    @Query("UPDATE user_statistics SET " +
            "days_active = days_active + 1, " +
            "last_active_at = NOW(), " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> incrementDaysActive(UUID userId);

    @Query("UPDATE user_statistics SET " +
            "loyalty_points = loyalty_points + :points, " +
            "updated_at = NOW() " +
            "WHERE user_id = :userId")
    Mono<Void> addLoyaltyPoints(UUID userId, Integer points);

    @Query("SELECT " +
            "COUNT(*) as total_users, " +
            "SUM(total_orders) as total_orders_all, " +
            "SUM(total_spent) as total_spent_all, " +
            "AVG(average_order_value) as avg_order_value_all, " +
            "AVG(loyalty_points) as avg_loyalty_points " +
            "FROM user_statistics")
    Mono<GlobalStatisticsProjection> getGlobalStatistics();

    interface GlobalStatisticsProjection {
        Long getTotalUsers();
        Long getTotalOrdersAll();
        Double getTotalSpentAll();
        Double getAvgOrderValueAll();
        Double getAvgLoyaltyPoints();
    }

    @Query("SELECT loyalty_tier, COUNT(*) as count, AVG(loyalty_points) as avg_points " +
            "FROM user_statistics GROUP BY loyalty_tier")
    Flux<LoyaltyTierStatisticsProjection> getLoyaltyTierStatistics();

    interface LoyaltyTierStatisticsProjection {
        String getLoyaltyTier();
        Long getCount();
        Double getAvgPoints();
    }

    Mono<Void> deleteByUserId(UUID userId);
}
