package com.mygitgor.transaction_service.domain.port.outgoing;

import com.mygitgor.transaction_service.domain.model.Transaction;
import reactor.core.CorePublisher;
import reactor.core.publisher.Mono;

public interface KafkaEventPort {
    Mono<Void> sendTransactionCreatedEvent(Transaction transaction);
    Mono<Void> sendTransactionCompletedEvent(Transaction transaction);
    Mono<Void> sendTransactionRefundedEvent(Transaction transaction);
}
