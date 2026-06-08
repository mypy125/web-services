package com.mygitgor.auth_service.infrastrucrure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String USER_REGISTERED_TOPIC = "user.registered";
    public static final String USER_LOGGED_IN_TOPIC = "user.logged.in";
    public static final String USER_LOGGED_OUT_TOPIC = "user.logged.out";
    public static final String TOKEN_REFRESHED_TOPIC = "token.refreshed";
    public static final String OTP_GENERATED_TOPIC = "otp.generated";
    public static final String OTP_VERIFIED_SUCCESS_TOPIC = "otp.verified.success";
    public static final String OTP_VERIFIED_FAILURE_TOPIC = "otp.verified.failure";

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name(USER_REGISTERED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userLoggedInTopic() {
        return TopicBuilder.name(USER_LOGGED_IN_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userLoggedOutTopic() {
        return TopicBuilder.name(USER_LOGGED_OUT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic tokenRefreshedTopic() {
        return TopicBuilder.name(TOKEN_REFRESHED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public NewTopic otpGeneratedTopic() {
        return TopicBuilder.name(OTP_GENERATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic otpVerifiedSuccessTopic() {
        return TopicBuilder.name(OTP_VERIFIED_SUCCESS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic otpVerifiedFailureTopic() {
        return TopicBuilder.name(OTP_VERIFIED_FAILURE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}