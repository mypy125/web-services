package com.mygitgor.user_service.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.mygitgor.user_service.infrastructure.kafka.KafkaTopics.*;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.topic.partitions:3}")
    private int partitions;

    @Value("${spring.kafka.topic.replicas:1}")
    private int replicas;

    @Bean
    public NewTopic userCreatedTopic() {
        return createTopic(USER_CREATED_TOPIC);
    }

    @Bean
    public NewTopic userUpdatedTopic() {
        return createTopic(USER_UPDATED_TOPIC);
    }

    @Bean
    public NewTopic userDeletedTopic() {
        return createTopic(USER_DELETED_TOPIC);
    }

    @Bean
    public NewTopic emailVerifiedTopic() {
        return createTopic(EMAIL_VERIFIED_TOPIC);
    }

    @Bean
    public NewTopic passwordChangedTopic() {
        return createTopic(PASSWORD_CHANGED_TOPIC);
    }

    @Bean
    public NewTopic userStatusChangedTopic() {
        return createTopic(USER_STATUS_CHANGED_TOPIC);
    }

    @Bean
    public NewTopic userRoleChangedTopic() {
        return createTopic(USER_ROLE_CHANGED_TOPIC);
    }

    @Bean
    public NewTopic userOrderStatsTopic() {
        return createTopic(USER_ORDER_STATS_UPDATED_TOPIC);
    }

    @Bean
    public NewTopic userLoyaltyTopic() {
        return createTopic(USER_LOYALTY_UPDATED_TOPIC);
    }

    @Bean
    public NewTopic userReviewTopic() {
        return createTopic(USER_REVIEW_UPDATED_TOPIC);
    }

    @Bean
    public NewTopic userActivityTopic() {
        return createTopic(USER_ACTIVITY_UPDATED_TOPIC);
    }

    @Bean
    public NewTopic userCouponTopic() {
        return createTopic(USER_COUPON_UPDATED_TOPIC);
    }

    @Bean
    public NewTopic userProductTopic() {
        return createTopic(USER_PRODUCT_UPDATED_TOPIC);
    }

    @Bean
    public NewTopic userWishlistTopic() {
        return createTopic(USER_WISHLIST_UPDATED_TOPIC);
    }

    @Bean
    public NewTopic userCartsTopic() {
        return createTopic(USER_CART_UPDATED_TOPIC);
    }

    private NewTopic createTopic(String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
