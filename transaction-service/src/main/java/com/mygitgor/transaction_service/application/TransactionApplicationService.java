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
                                                   Double amount,
                                                   Double tax,
                                                   Double commission,
                                                   Double shippingCost,
                                                   Double discount
    ) {
        log.info("Creating sale transaction for seller: {}, order: {}", sellerId, orderId);

        return Mono.fromCallable(() -> Transaction.createSale(
                        sellerId, customerId, orderId, amount, tax, commission, shippingCost, discount
                ))
                .flatMap(transactionRepository::save)
                .doOnSuccess(transaction -> {
                    eventPort.sendTransactionCreatedEvent(transaction).subscribe();
                    log.info("Sale transaction created: {}", transaction.getTransactionId());
                });
    }

    @Transactional
    public Mono<Transaction> createRefundTransaction(SellerId sellerId,
                                                     UserId customerId,
                                                     OrderId orderId,
                                                     Double amount,
                                                     String reason
    ) {
        log.info("Creating refund transaction for seller: {}, order: {}", sellerId, orderId);

        return Mono.fromCallable(() -> Transaction.createRefund(
                        sellerId, customerId, orderId, amount, reason
                ))
                .flatMap(transactionRepository::save)
                .doOnSuccess(transaction -> {
                    eventPort.sendTransactionCreatedEvent(transaction).subscribe();
                    log.info("Refund transaction created: {}", transaction.getTransactionId());
                });
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
                .doOnSuccess(transaction -> {
                    eventPort.sendTransactionCompletedEvent(transaction).subscribe();
                    log.info("Transaction completed: {}", transactionId);
                });
    }

    @Transactional
    public Mono<Transaction> refundTransaction(TransactionId transactionId) {
        log.info("Refunding transaction: {}", transactionId);

        return transactionRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new DomainException("Transaction not found: " + transactionId)))
                .flatMap(transaction -> {
                    transaction.refund();
                    return transactionRepository.save(transaction);
                })
                .doOnSuccess(transaction -> {
                    eventPort.sendTransactionRefundedEvent(transaction).subscribe();
                    log.info("Transaction refunded: {}", transactionId);
                });
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
}
