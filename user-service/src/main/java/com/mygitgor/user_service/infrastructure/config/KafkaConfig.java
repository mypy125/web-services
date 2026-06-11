package com.mygitgor.user_service.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic userOrderStatsTopic() {
        return TopicBuilder.name("user.order.stats.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userLoyaltyTopic() {
        return TopicBuilder.name("user.loyalty.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userReviewTopic() {
        return TopicBuilder.name("user.review.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userActivityTopic() {
        return TopicBuilder.name("user.activity.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userCouponTopic() {
        return TopicBuilder.name("user.coupon.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userProductTopic() {
        return TopicBuilder.name("user.product.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userWishlistTopic() {
        return TopicBuilder.name("user.wishlist.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userCartsTopic() {
        return TopicBuilder.name("user.cart.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
