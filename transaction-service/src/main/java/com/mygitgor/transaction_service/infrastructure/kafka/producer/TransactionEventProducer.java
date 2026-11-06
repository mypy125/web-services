package com.mygitgor.transaction_service.infrastructure.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.transaction_service.domain.model.Transaction;
import com.mygitgor.transaction_service.domain.port.outgoing.KafkaEventPort;
import com.mygitgor.transaction_service.infrastructure.kafka.event.TransactionCompletedEvent;
import com.mygitgor.transaction_service.infrastructure.kafka.event.TransactionCreatedEvent;
import com.mygitgor.transaction_service.infrastructure.kafka.event.TransactionFailedEvent;
import com.mygitgor.transaction_service.infrastructure.kafka.event.TransactionRefundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

import static com.mygitgor.transaction_service.infrastructure.kafka.KafkaTopics.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer implements KafkaEventPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> sendTransactionCreatedEvent(Transaction transaction) {
        return sendEvent(
                TRANSACTION_CREATED_TOPIC,
                transaction.getTransactionId().toString(),
                mapToCreatedEvent(transaction)
        );
    }

    @Override
    public Mono<Void> sendTransactionCompletedEvent(Transaction transaction) {
        return sendEvent(
                TRANSACTION_COMPLETED_TOPIC,
                transaction.getTransactionId().toString(),
                mapToCompletedEvent(transaction)
        );
    }

    @Override
    public Mono<Void> sendTransactionRefundedEvent(Transaction transaction) {
        return sendEvent(
                TRANSACTION_REFUNDED_TOPIC,
                transaction.getTransactionId().toString(),
                mapToRefundedEvent(transaction)
        );
    }

    @Override
    public Mono<Void> sendTransactionFailedEvent(Transaction transaction) {
        return sendEvent(
                TRANSACTION_FAILED_TOPIC,
                transaction.getTransactionId().toString(),
                mapToFailedEvent(transaction)
        );
    }

    private Mono<Void> sendEvent(String topic, String key, Object event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(json -> Mono.fromFuture(
                        kafkaTemplate.send(topic, key, json)
                ))
                .doOnSuccess(result -> log.info(
                        "Event sent to topic: {}, key: {}, partition: {}, offset: {}",
                        topic, key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                ))
                .doOnError(error -> log.error(
                        "Failed to send event to topic: {}, key: {}, error: {}",
                        topic, key, error.getMessage()
                ))
                .then();
    }

    private TransactionCreatedEvent mapToCreatedEvent(Transaction transaction) {
        return TransactionCreatedEvent.builder()
                .transactionId(transaction.getTransactionId().toString())
                .sellerId(transaction.getSellerId().toString())
                .customerId(transaction.getCustomerId() != null
                        ? transaction.getCustomerId().toString() : null)
                .orderId(transaction.getOrderId().toString())
                .type(transaction.getType() != null ? transaction.getType().name() : null)
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .amount(transaction.getAmount())
                .tax(transaction.getTax())
                .commission(transaction.getCommission())
                .shippingCost(transaction.getShippingCost())
                .discount(transaction.getDiscount())
                .netAmount(transaction.getNetAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private TransactionCompletedEvent mapToCompletedEvent(Transaction transaction) {
        return TransactionCompletedEvent.builder()
                .transactionId(transaction.getTransactionId().toString())
                .sellerId(transaction.getSellerId().toString())
                .orderId(transaction.getOrderId().toString())
                .amount(transaction.getAmount())
                .netAmount(transaction.getNetAmount())
                .currency(transaction.getCurrency())
                .completedAt(transaction.getCompletedAt())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private TransactionRefundedEvent mapToRefundedEvent(Transaction transaction) {
        return TransactionRefundedEvent.builder()
                .transactionId(transaction.getTransactionId().toString())
                .sellerId(transaction.getSellerId().toString())
                .customerId(transaction.getCustomerId() != null
                        ? transaction.getCustomerId().toString() : null)
                .orderId(transaction.getOrderId().toString())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .refundedAt(transaction.getRefundedAt())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private TransactionFailedEvent mapToFailedEvent(Transaction transaction) {
        return TransactionFailedEvent.builder()
                .transactionId(transaction.getTransactionId().toString())
                .sellerId(transaction.getSellerId().toString())
                .orderId(transaction.getOrderId().toString())
                .amount(transaction.getAmount())
                .reason(transaction.getNotes())
                .failedAt(transaction.getUpdatedAt())
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
