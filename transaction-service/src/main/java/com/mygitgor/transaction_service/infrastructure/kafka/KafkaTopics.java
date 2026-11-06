package com.mygitgor.transaction_service.infrastructure.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaTopics {
    public static final String TRANSACTION_CREATED_TOPIC = "transaction.created";
    public static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    public static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";
    public static final String TRANSACTION_FAILED_TOPIC = "transaction.failed";
}
