package com.mygitgor.transaction_service.domain.port.outgoing;

import com.mygitgor.transaction_service.domain.model.Transaction;
import com.mygitgor.transaction_service.domain.model.TransactionStatistics;
import com.mygitgor.transaction_service.domain.model.TransactionStatus;
import com.mygitgor.transaction_service.domain.model.TransactionType;
import com.mygitgor.transaction_service.shared.valueobject.OrderId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.TransactionId;
import com.mygitgor.transaction_service.shared.valueobject.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepositoryPort {
    Mono<Transaction> save(Transaction transaction);
    Mono<Transaction> updateStatus(TransactionId transactionId, TransactionStatus status);
    Mono<Transaction> completeTransaction(TransactionId transactionId);
    Mono<Transaction> failTransaction(TransactionId transactionId, String reason);
    Mono<Transaction> refundTransaction(TransactionId transactionId);
    Mono<Void> deleteById(TransactionId transactionId);

    Mono<Transaction> findById(TransactionId transactionId);
    Mono<Transaction> findByOrderId(OrderId orderId);
    Mono<Transaction> findByReferenceNumber(String referenceNumber);
    Flux<Transaction> findBySellerId(SellerId sellerId, int page, int size);
    Flux<Transaction> findByCustomerId(UserId customerId, int page, int size);
    Flux<Transaction> findBySellerIdAndType(SellerId sellerId, TransactionType type, int page, int size);
    Flux<Transaction> findBySellerIdAndStatus(SellerId sellerId, TransactionStatus status, int page, int size);
    Flux<Transaction> findBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );
    Flux<Transaction> findByStatus(TransactionStatus status, int page, int size);
    Flux<Transaction> findByType(TransactionType type, int page, int size);
    Flux<Transaction> findAllBySellerId(SellerId sellerId, int page, int size);
    Flux<Transaction> findBySellerIdAndTypeAndStatus(
            SellerId sellerId,
            TransactionType type,
            TransactionStatus status,
            int page,
            int size
    );
    Flux<Transaction> findByIds(List<TransactionId> transactionIds);

    Mono<Long> countBySellerId(SellerId sellerId);
    Mono<Long> countBySellerIdAndType(SellerId sellerId, TransactionType type);
    Mono<Long> countBySellerIdAndStatus(SellerId sellerId, TransactionStatus status);
    Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<Long> countCompletedTransactions();
    Mono<Long> countPendingTransactions();

    Mono<Double> sumAmountBySellerId(SellerId sellerId);
    Mono<Double> sumAmountBySellerIdAndType(SellerId sellerId, TransactionType type);
    Mono<Double> sumAmountBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Mono<Double> sumCommissionBySellerId(SellerId sellerId);
    Mono<Double> sumRefundsBySellerId(SellerId sellerId);

    Mono<TransactionStatistics> getTransactionStatistics(SellerId sellerId);
    Mono<TransactionStatistics> getTransactionStatistics(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
