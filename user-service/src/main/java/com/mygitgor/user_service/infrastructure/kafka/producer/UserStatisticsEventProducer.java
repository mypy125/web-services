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
        OrderStatsUpdatedEvent event = new OrderStatsUpdatedEvent(
                userId.getValue().toString(),
                stats.getTotalOrders(),
                stats.getTotalSpent(),
                stats.getAverageOrderValue(),
                stats.getLastOrderDate(),
                LocalDateTime.now()
        );

        return sendEvent(TOPIC_ORDER_STATS_UPDATED, event);
    }

    public Mono<Void> sendReviewStatsUpdatedEvent(UserId userId, UserStatistics stats) {
        ReviewStatsUpdatedEvent event = new ReviewStatsUpdatedEvent(
                userId.getValue().toString(),
                stats.getTotalReviews(),
                stats.getAverageRating(),
                LocalDateTime.now()
        );

        return sendEvent(TOPIC_REVIEW_STATS_UPDATED, event);
    }

    public Mono<Void> sendLoyaltyUpdatedEvent(UserId userId, UserStatistics stats, String oldTier) {
        LoyaltyUpdatedEvent event = new LoyaltyUpdatedEvent(
                userId.getValue().toString(),
                oldTier,
                stats.getLoyaltyTier(),
                stats.getLoyaltyPoints(),
                stats.getLoyaltyPoints() - getPreviousPoints(stats),
                LocalDateTime.now()
        );

        return sendEvent(TOPIC_LOYALTY_UPDATED, event);
    }

    public Mono<Void> sendActivityUpdatedEvent(UserId userId, UserStatistics stats) {
        ActivityUpdatedEvent event = new ActivityUpdatedEvent(
                userId.getValue().toString(),
                stats.getLastActiveAt(),
                stats.getDaysActive(),
                stats.getConsecutiveLoginDays(),
                LocalDateTime.now()
        );

        return sendEvent(TOPIC_ACTIVITY_UPDATED, event);
    }

    public Mono<Void> sendCouponStatsUpdatedEvent(UserId userId, UserStatistics stats, Double discountAmount, String couponCode) {
        CouponStatsUpdatedEvent event = new CouponStatsUpdatedEvent(
                userId.getValue().toString(),
                stats.getCouponsUsed(),
                stats.getTotalDiscountReceived(),
                couponCode,
                discountAmount,
                LocalDateTime.now()
        );

        return sendEvent(TOPIC_COUPON_STATS_UPDATED, event);
    }

    public Mono<Void> sendProductStatsUpdatedEvent(UserId userId, UserStatistics stats, String productId, String category) {
        ProductStatsUpdatedEvent event = new ProductStatsUpdatedEvent(
                userId.getValue().toString(),
                stats.getTotalProductsPurchased(),
                stats.getMostPurchasedCategory(),
                stats.getFavoriteProductId(),
                stats.getFavoriteProductName(),
                productId,
                category,
                LocalDateTime.now()
        );

        return sendEvent(TOPIC_PRODUCT_STATS_UPDATED, event);
    }

    public Mono<Void> sendWishlistStatsUpdatedEvent(UserId userId, Integer wishlistItemsCount) {
        WishlistStatsUpdatedEvent event = new WishlistStatsUpdatedEvent(
                userId.getValue().toString(),
                wishlistItemsCount,
                LocalDateTime.now()
        );

        return sendEvent(TOPIC_WISHLIST_STATS_UPDATED, event);
    }

    public Mono<Void> sendCartStatsUpdatedEvent(UserId userId, Integer cartItemsCount) {
        CartStatsUpdatedEvent event = new CartStatsUpdatedEvent(
                userId.getValue().toString(),
                cartItemsCount,
                LocalDateTime.now()
        );

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
