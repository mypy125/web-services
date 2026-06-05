package com.mygitgor.auth_service.infrastrucrure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public Mono<Void> sendEvent(String topic, Object event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(json -> Mono.fromFuture(kafkaTemplate.send(topic, json))
                        .doOnSuccess(result -> log.debug("Event sent to topic {}: {}", topic, event))
                        .doOnError(error -> log.error("Failed to send event to topic {}: {}", topic, error.getMessage()))
                        .then());
    }
}
