package com.mygitgor.user_service.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

import static com.mygitgor.user_service.infrastructure.kafka.KafkaTopics.*;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

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

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages("com.mygitgor.user_service.infrastructure.kafka.event");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
