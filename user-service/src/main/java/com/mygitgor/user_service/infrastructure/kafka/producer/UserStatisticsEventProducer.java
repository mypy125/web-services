package com.mygitgor.user_service.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.infrastructure.kafka.event.*;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatisticsEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_ORDER_STATS_UPDATED = "user.order.stats.updated";
    private static final String TOPIC_REVIEW_STATS_UPDATED = "user.review.stats.updated";
    private static final String TOPIC_LOYALTY_UPDATED = "user.loyalty.updated";
    private static final String TOPIC_ACTIVITY_UPDATED = "user.activity.updated";
    private static final String TOPIC_COUPON_STATS_UPDATED = "user.coupon.stats.updated";
    private static final String TOPIC_PRODUCT_STATS_UPDATED = "user.product.stats.updated";
    private static final String TOPIC_WISHLIST_STATS_UPDATED = "user.wishlist.stats.updated";
    private static final String TOPIC_CART_STATS_UPDATED = "user.cart.stats.updated";


    public Mono<Void> sendOrderStatsUpdatedEvent(UserId userId, UserStatistics stats) {
        OrderStatsUpdatedEvent event = OrderStatsUpdatedEvent.builder()
                .userId(userId.toString())
                .totalOrders(stats.getTotalOrders())
                .totalSpent(stats.getTotalSpent())
                .averageOrderValue(stats.getAverageOrderValue())
                .lastOrderDate(stats.getLastOrderDate())
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(TOPIC_ORDER_STATS_UPDATED, event);
    }

    public Mono<Void> sendReviewStatsUpdatedEvent(UserId userId, UserStatistics stats) {
        ReviewStatsUpdatedEvent event = ReviewStatsUpdatedEvent.builder()
                .userId(userId.toString())
                .totalReviews(stats.getTotalReviews())
                .averageRating(stats.getAverageRating())
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(TOPIC_REVIEW_STATS_UPDATED, event);
    }

    public Mono<Void> sendLoyaltyUpdatedEvent(UserId userId, UserStatistics stats, String oldTier) {
        LoyaltyUpdatedEvent event = LoyaltyUpdatedEvent.builder()
                .userId(userId.toString())
                .oldTier(oldTier)
                .newTier(stats.getLoyaltyTier())
                .loyaltyPoints(stats.getLoyaltyPoints())
                .pointsChange(stats.getLoyaltyPoints() - getPreviousPoints(stats))
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(TOPIC_LOYALTY_UPDATED, event);
    }

    public Mono<Void> sendActivityUpdatedEvent(UserId userId, UserStatistics stats) {
        ActivityUpdatedEvent event = ActivityUpdatedEvent.builder()
                .userId(userId.toString())
                .lastActiveAt(stats.getLastActiveAt())
                .daysActive(stats.getDaysActive())
                .consecutiveLoginDays(stats.getConsecutiveLoginDays())
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(TOPIC_ACTIVITY_UPDATED, event);
    }

    public Mono<Void> sendCouponStatsUpdatedEvent(UserId userId, UserStatistics stats, Double discountAmount, String couponCode) {
        CouponStatsUpdatedEvent event = CouponStatsUpdatedEvent.builder()
                .userId(userId.toString())
                .couponsUsed(stats.getCouponsUsed())
                .totalDiscountReceived(stats.getTotalDiscountReceived())
                .lastUsedCouponCode(couponCode)
                .lastUsedDiscountAmount(discountAmount)
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(TOPIC_COUPON_STATS_UPDATED, event);
    }

    public Mono<Void> sendProductStatsUpdatedEvent(UserId userId, UserStatistics stats, String productId, String category) {
        ProductStatsUpdatedEvent event = ProductStatsUpdatedEvent.builder()
                .userId(userId.toString())
                .totalProductsPurchased(stats.getTotalProductsPurchased())
                .mostPurchasedCategory(stats.getMostPurchasedCategory())
                .favoriteProductId(stats.getFavoriteProductId())
                .favoriteProductName(stats.getFavoriteProductName())
                .lastPurchasedProductId(productId)
                .lastPurchasedCategory(category)
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(TOPIC_PRODUCT_STATS_UPDATED, event);
    }

    public Mono<Void> sendWishlistStatsUpdatedEvent(UserId userId, Integer wishlistItemsCount) {
        WishlistStatsUpdatedEvent event = WishlistStatsUpdatedEvent.builder()
                .userId(userId.toString())
                .wishlistItemsCount(wishlistItemsCount)
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(TOPIC_WISHLIST_STATS_UPDATED, event);
    }

    public Mono<Void> sendCartStatsUpdatedEvent(UserId userId, Integer cartItemsCount) {
        CartStatsUpdatedEvent event = CartStatsUpdatedEvent.builder()
                .userId(userId.toString())
                .cartItemsCount(cartItemsCount)
                .occurredAt(LocalDateTime.now())
                .build();

        return sendEvent(TOPIC_CART_STATS_UPDATED, event);
    }

    private Mono<Void> sendEvent(String topic, Object event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(json -> Mono.fromFuture(() -> kafkaTemplate.send(topic, json))
                        .doOnSuccess(result -> {
                            log.debug("Event sent to topic {}: {}", topic, event);
                        })
                        .doOnError(error -> {
                            log.error("Failed to send event to topic {}: {}", topic, error.getMessage());
                        })
                )
                .then();
    }

    private Integer getPreviousPoints(UserStatistics stats) {
        return 0;
    }
}
