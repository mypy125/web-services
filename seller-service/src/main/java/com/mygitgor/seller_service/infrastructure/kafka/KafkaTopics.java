package com.mygitgor.seller_service.infrastructure.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaTopics {
    public static final String SELLER_EVENTS = "seller-events-topic";
    public static final String SELLER_TRANSACTIONS = "seller-transactions-topic";
    public static final String SELLER_PRODUCTS = "seller-products-topic";
    public static final String SELLER_REPORTS = "seller-reports-topic";
}
