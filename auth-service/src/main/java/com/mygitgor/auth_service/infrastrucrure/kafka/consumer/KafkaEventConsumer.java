package com.mygitgor.auth_service.infrastrucrure.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    @KafkaListener(topics = "user.registered", groupId = "auth-service-group")
    public void consumeUserRegisteredEvent(String message) {
        log.info("Received user registered event: {}", message);
    }

    @KafkaListener(topics = "user.logged.in", groupId = "auth-service-group")
    public void consumeUserLoggedInEvent(String message) {
        log.info("Received user logged in event: {}", message);
    }
}
