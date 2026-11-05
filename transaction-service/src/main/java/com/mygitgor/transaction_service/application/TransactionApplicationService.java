package com.mygitgor.transaction_service.application;

import com.mygitgor.transaction_service.domain.model.Transaction;
import com.mygitgor.transaction_service.domain.model.TransactionStatistics;
import com.mygitgor.transaction_service.domain.port.outgoing.KafkaEventPort;
import com.mygitgor.transaction_service.domain.port.outgoing.TransactionRepositoryPort;
import com.mygitgor.transaction_service.infrastructure.kafka.producer.TransactionEventProducer;
import com.mygitgor.transaction_service.shared.exception.DomainException;
import com.mygitgor.transaction_service.shared.valueobject.OrderId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.TransactionId;
import com.mygitgor.transaction_service.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionApplicationService {
    private final TransactionRepositoryPort transactionRepository;
    private final KafkaEventPort eventPort;

    @Transactional
    public Mono<Transaction> createSaleTransaction(SellerId sellerId,
                                                   UserId customerId,
                                                   OrderId orderId,
                                                   BigDecimal amount,
                                                   BigDecimal tax,
                                                   BigDecimal commission,
                                                   BigDecimal shippingCost,
                                                   BigDecimal discount
    ) {
        log.info("Creating sale transaction for seller: {}, order: {}", sellerId, orderId);

        return Mono.fromCallable(() -> Transaction.createSale(
                        sellerId, customerId, orderId, amount, tax, commission, shippingCost, discount
                ))
                .flatMap(transactionRepository::save)
                .flatMap(this::emitTransactionCreatedEvent)
                .doOnSuccess(t -> log.info("Sale transaction created: {}", t.getTransactionId()))
                .doOnError(e -> log.error("Failed to create sale transaction: {}", e.getMessage()));
    }

    @Transactional
    public Mono<Transaction> createRefundTransaction(SellerId sellerId,
                                                     UserId customerId,
                                                     OrderId orderId,
                                                     BigDecimal amount,
                                                     String reason
    ) {
        log.info("Creating refund transaction for seller: {}, order: {}", sellerId, orderId);

        return Mono.fromCallable(() -> Transaction.createRefund(
                        sellerId, customerId, orderId, amount, reason
                ))
                .flatMap(transactionRepository::save)
                .flatMap(this::emitTransactionCreatedEvent)
                .doOnSuccess(t -> log.info("Refund transaction created: {}", t.getTransactionId()))
                .doOnError(e -> log.error("Failed to create refund transaction: {}", e.getMessage()));
    }

    @Transactional
    public Mono<Transaction> completeTransaction(TransactionId transactionId) {
        log.info("Completing transaction: {}", transactionId);

        return transactionRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new DomainException("Transaction not found: " + transactionId)))
                .flatMap(transaction -> {
                    transaction.complete();
                    return transactionRepository.save(transaction);
                })
                .flatMap(this::emitTransactionCompletedEvent)
                .doOnSuccess(t -> log.info("Transaction completed: {}", transactionId))
                .doOnError(e -> log.error("Failed to complete transaction: {}", e.getMessage()));
    }

    @Transactional
    public Mono<Transaction> refundTransaction(TransactionId transactionId, String reason) {
        log.info("Refunding transaction: {}", transactionId);

        return transactionRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new DomainException("Transaction not found: " + transactionId)))
                .flatMap(original -> {
                    original.markAsRefunded();
                    return transactionRepository.save(original)
                            .flatMap(savedOriginal -> {
                                Transaction refund = savedOriginal.emitRefundTransaction(reason);
                                return transactionRepository.save(refund)
                                        .thenReturn(savedOriginal);
                            });
                })
                .flatMap(this::emitTransactionRefundedEvent)
                .doOnSuccess(t -> log.info("Transaction refunded: {}", transactionId))
                .doOnError(e -> log.error("Failed to refund transaction: {}", e.getMessage()));
    }

    @Transactional
    public Mono<Transaction> failTransaction(TransactionId transactionId, String reason) {
        log.info("Failing transaction: {}, reason: {}", transactionId, reason);

        return transactionRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new DomainException("Transaction not found: " + transactionId)))
                .flatMap(transaction -> {
                    transaction.fail(reason);
                    return transactionRepository.save(transaction);
                })
                .flatMap(this::emitTransactionFailedEvent)
                .doOnSuccess(t -> log.info("Transaction failed: {}", transactionId))
                .doOnError(e -> log.error("Failed to fail transaction: {}", e.getMessage()));
    }

    public Mono<Transaction> getTransactionById(TransactionId transactionId) {
        return transactionRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new DomainException("Transaction not found: " + transactionId)));
    }

    public Flux<Transaction> getTransactionsBySellerId(SellerId sellerId, int page, int size) {
        return transactionRepository.findBySellerId(sellerId, page, size);
    }

    public Mono<TransactionStatistics> getTransactionStatistics(SellerId sellerId) {
        return transactionRepository.getTransactionStatistics(sellerId);
    }

    private Mono<Transaction> emitTransactionCreatedEvent(Transaction transaction) {
        return eventPort.sendTransactionCreatedEvent(transaction)
                .thenReturn(transaction)
                .onErrorResume(e -> {
                    log.error("Failed to emit transaction created event: {}", e.getMessage());
                    return Mono.just(transaction);
                });
    }

    private Mono<Transaction> emitTransactionCompletedEvent(Transaction transaction) {
        return eventPort.sendTransactionCompletedEvent(transaction)
                .thenReturn(transaction)
                .onErrorResume(e -> {
                    log.error("Failed to emit transaction completed event: {}", e.getMessage());
                    return Mono.just(transaction);
                });
    }

    private Mono<Transaction> emitTransactionRefundedEvent(Transaction transaction) {
        return eventPort.sendTransactionRefundedEvent(transaction)
                .thenReturn(transaction)
                .onErrorResume(e -> {
                    log.error("Failed to emit transaction refunded event: {}", e.getMessage());
                    return Mono.just(transaction);
                });
    }

    private Mono<Transaction> emitTransactionFailedEvent(Transaction transaction) {
        return eventPort.sendTransactionFailedEvent(transaction)
                .thenReturn(transaction)
                .onErrorResume(e -> {
                    log.error("Failed to emit transaction failed event: {}", e.getMessage());
                    return Mono.just(transaction);
                });
    }
}
