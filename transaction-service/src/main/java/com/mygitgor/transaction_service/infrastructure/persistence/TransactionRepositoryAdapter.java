package com.mygitgor.transaction_service.infrastructure.persistence;

import com.mygitgor.transaction_service.domain.model.Transaction;
import com.mygitgor.transaction_service.domain.model.TransactionStatistics;
import com.mygitgor.transaction_service.domain.model.valueobject.TransactionStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.TransactionType;
import com.mygitgor.transaction_service.domain.port.outgoing.TransactionRepositoryPort;
import com.mygitgor.transaction_service.infrastructure.persistence.entity.TransactionEntity;
import com.mygitgor.transaction_service.infrastructure.persistence.mapper.TransactionPersistenceMapper;
import com.mygitgor.transaction_service.infrastructure.persistence.repository.TransactionR2dbcRepository;
import com.mygitgor.transaction_service.shared.valueobject.OrderId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.TransactionId;
import com.mygitgor.transaction_service.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {
    private final TransactionR2dbcRepository repository;
    private final TransactionPersistenceMapper mapper;

    @Override
    public Mono<Transaction> save(Transaction transaction) {
        log.debug("Saving transaction: {}", transaction.getTransactionId());
        TransactionEntity entity = mapper.toEntity(transaction);
        return repository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(t -> log.debug("Transaction saved: {}", t.getTransactionId()))
                .doOnError(e -> log.error("Failed to save transaction: {}", e.getMessage()));
    }

    @Override
    public Mono<Transaction> updateStatus(TransactionId transactionId, TransactionStatus status) {
        return repository.findById(UUID.fromString(transactionId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found: " + transactionId)))
                .flatMap(entity -> {
                    TransactionEntity updatedEntity = entity
                            .withStatus(status.name())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updatedEntity);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Transaction> completeTransaction(TransactionId transactionId) {
        return repository.findById(UUID.fromString(transactionId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found: " + transactionId)))
                .flatMap(entity -> {
                    TransactionEntity compiledEntity = entity
                            .withStatus(TransactionStatus.COMPLETED.name())
                            .withCompletedAt(LocalDateTime.now())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(compiledEntity);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Transaction> failTransaction(TransactionId transactionId, String reason) {
        return repository.findById(UUID.fromString(transactionId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found: " + transactionId)))
                .flatMap(entity -> {
                    TransactionEntity failTransaction = entity
                            .withStatus(TransactionStatus.FAILED.name())
                            .withNotes(reason)
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(failTransaction);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Transaction> refundTransaction(TransactionId transactionId) {
        return repository.findById(UUID.fromString(transactionId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found: " + transactionId)))
                .flatMap(entity -> {
                    TransactionEntity refundTransaction = entity
                            .withStatus(TransactionStatus.REFUNDED.name())
                            .withRefundedAt(LocalDateTime.now())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(refundTransaction);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(TransactionId transactionId) {
        return repository.deleteById(UUID.fromString(transactionId.toString()));
    }

    @Override
    public Mono<Transaction> findById(TransactionId transactionId) {
        return repository.findById(UUID.fromString(transactionId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Transaction> findByOrderId(OrderId orderId) {
        return repository.findByOrderId(UUID.fromString(orderId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Transaction> findByReferenceNumber(String referenceNumber) {
        return repository.findByReferenceNumber(referenceNumber)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findBySellerId(SellerId sellerId, int page, int size) {
        return repository.findBySellerId(
                        UUID.fromString(sellerId.toString()),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findByCustomerId(UserId customerId, int page, int size) {
        return repository.findByCustomerId(
                        UUID.fromString(customerId.toString()),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findBySellerIdAndType(SellerId sellerId, TransactionType type, int page, int size) {
        return repository.findBySellerIdAndType(
                        UUID.fromString(sellerId.toString()),
                        type.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findBySellerIdAndStatus(SellerId sellerId, TransactionStatus status, int page, int size) {
        return repository.findBySellerIdAndStatus(
                        UUID.fromString(sellerId.toString()),
                        status.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        return repository.findBySellerIdAndCreatedAtBetween(
                        UUID.fromString(sellerId.toString()),
                        startDate,
                        endDate,
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findByStatus(TransactionStatus status, int page, int size) {
        return repository.findByStatus(status.name(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findByType(TransactionType type, int page, int size) {
        return repository.findByType(type.name(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findAllBySellerId(SellerId sellerId, int page, int size) {
        return repository.findBySellerId(
                        UUID.fromString(sellerId.toString()),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findBySellerIdAndTypeAndStatus(SellerId sellerId, TransactionType type, TransactionStatus status, int page, int size) {
        return repository.findBySellerIdAndTypeAndStatus(
                        UUID.fromString(sellerId.toString()),
                        type.name(),
                        status.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transaction> findByIds(List<TransactionId> transactionIds) {
        List<UUID> uuids = transactionIds.stream()
                .map(id -> UUID.fromString(id.toString()))
                .toList();
        return repository.findByIdIn(uuids)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countBySellerId(SellerId sellerId) {
        return repository.countBySellerId(UUID.fromString(sellerId.toString()));
    }

    @Override
    public Mono<Long> countBySellerIdAndType(SellerId sellerId, TransactionType type) {
        return repository.countBySellerIdAndType(
                UUID.fromString(sellerId.toString()), type.name());
    }

    @Override
    public Mono<Long> countBySellerIdAndStatus(SellerId sellerId, TransactionStatus status) {
        return repository.countBySellerIdAndStatus(
                UUID.fromString(sellerId.toString()), status.name());
    }

    @Override
    public Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.countBySellerIdAndCreatedAtBetween(
                UUID.fromString(sellerId.toString()), startDate, endDate);
    }

    @Override
    public Mono<Long> countCompletedTransactions() {
        return repository.countByStatus(TransactionStatus.COMPLETED.name());
    }

    @Override
    public Mono<Long> countPendingTransactions() {
        return repository.countByStatus(TransactionStatus.PENDING.name());
    }

    @Override
    public Mono<Double> sumAmountBySellerId(SellerId sellerId) {
        return repository.sumAmountBySellerId(UUID.fromString(sellerId.toString()))
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> sumAmountBySellerIdAndType(SellerId sellerId, TransactionType type) {
        return repository.sumAmountBySellerIdAndType(
                        UUID.fromString(sellerId.toString()), type.name())
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> sumAmountBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.sumAmountBySellerIdAndCreatedAtBetween(
                        UUID.fromString(sellerId.toString()), startDate, endDate)
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> sumCommissionBySellerId(SellerId sellerId) {
        return repository.sumCommissionBySellerId(UUID.fromString(sellerId.toString()))
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> sumRefundsBySellerId(SellerId sellerId) {
        return repository.sumRefundsBySellerId(
                        UUID.fromString(sellerId.toString()), TransactionType.REFUND.name())
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<TransactionStatistics> getTransactionStatistics(SellerId sellerId) {
        UUID sellerUuid = UUID.fromString(sellerId.toString());

        return Mono.zip(
                results -> {
                    Long total = (Long) results[0];
                    Long completed = (Long) results[1];
                    Long pending = (Long) results[2];
                    Long failed = (Long) results[3];
                    Long refunded = (Long) results[4];
                    Long cancelled = (Long) results[5];

                    BigDecimal totalAmount = toBigDecimal(results[6]);
                    BigDecimal totalCommission = toBigDecimal(results[7]);
                    BigDecimal totalRefunds = toBigDecimal(results[8]);
                    BigDecimal totalNetAmount = toBigDecimal(results[9]);

                    BigDecimal avgAmount = (total != null && total > 0)
                            ? totalAmount.divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return TransactionStatistics.builder()
                            .totalTransactions(total)
                            .completedTransactions(completed)
                            .pendingTransactions(pending)
                            .failedTransactions(failed)
                            .refundedTransactions(refunded)
                            .cancelledTransactions(cancelled)
                            .totalAmount(totalAmount)
                            .totalCommission(totalCommission)
                            .totalRefunds(totalRefunds)
                            .totalNetAmount(totalNetAmount)
                            .averageTransactionAmount(avgAmount)
                            .calculatedAt(LocalDateTime.now())
                            .build();
                },
                repository.countBySellerId(sellerUuid),
                repository.countBySellerIdAndStatus(sellerUuid, TransactionStatus.COMPLETED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, TransactionStatus.PENDING.name()),
                repository.countBySellerIdAndStatus(sellerUuid, TransactionStatus.FAILED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, TransactionStatus.REFUNDED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, TransactionStatus.CANCELLED.name()),
                repository.sumAmountBySellerId(sellerUuid).defaultIfEmpty(0.0),
                repository.sumCommissionBySellerId(sellerUuid).defaultIfEmpty(0.0),
                repository.sumRefundsBySellerId(sellerUuid, TransactionType.REFUND.name()).defaultIfEmpty(0.0),
                repository.sumNetAmountBySellerId(sellerUuid).defaultIfEmpty(0.0)
        );
    }

    @Override
    public Mono<TransactionStatistics> getTransactionStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        UUID sellerUuid = UUID.fromString(sellerId.toString());

        return Mono.zip(
                repository.countBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate),
                repository.sumAmountBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0),
                repository.sumCommissionBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0),
                repository.sumNetAmountBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0)
        ).map(tuple -> {
            Long total = tuple.getT1();
            BigDecimal totalAmount = toBigDecimal(tuple.getT2());
            BigDecimal totalCommission = toBigDecimal(tuple.getT3());
            BigDecimal totalNetAmount = toBigDecimal(tuple.getT4());

            BigDecimal avgAmount = total > 0
                    ? totalAmount.divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return TransactionStatistics.builder()
                    .totalTransactions(total)
                    .totalAmount(totalAmount)
                    .totalCommission(totalCommission)
                    .totalNetAmount(totalNetAmount)
                    .averageTransactionAmount(avgAmount)
                    .calculatedAt(LocalDateTime.now())
                    .build();
        });
    }

    private BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case null -> BigDecimal.ZERO;
            case BigDecimal bigDecimal -> bigDecimal;
            case Double v -> BigDecimal.valueOf(v);
            case Long l -> BigDecimal.valueOf(l);
            default -> new BigDecimal(value.toString());
        };
    }
}
