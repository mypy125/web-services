package com.mygitgor.transaction_service.infrastructure.persistence.repository;

import com.mygitgor.transaction_service.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionR2dbcRepository extends ReactiveCrudRepository<TransactionEntity, UUID> {
    Flux<TransactionEntity> findAllBySellerId(UUID sellerId, Pageable pageable);
    Flux<TransactionEntity> findAllBySellerIdAndStatus(UUID sellerId, String status, Pageable pageable);
    Mono<TransactionEntity> findByReferenceNumber(String referenceNumber);
    Mono<TransactionEntity> findByOrderId(UUID orderId);
    Flux<TransactionEntity> findBySellerId(UUID sellerId, Pageable pageable);
    Flux<TransactionEntity> findByCustomerId(UUID customerId, Pageable pageable);
    Flux<TransactionEntity> findBySellerIdAndType(UUID sellerId, String type, Pageable pageable);
    Flux<TransactionEntity> findBySellerIdAndStatus(UUID sellerId, String status, Pageable pageable);

    @Query("SELECT * FROM transactions WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Flux<TransactionEntity> findBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Flux<TransactionEntity> findByStatus(String status, Pageable pageable);
    Flux<TransactionEntity> findByType(String type, Pageable pageable);
    Flux<TransactionEntity> findBySellerIdAndTypeAndStatus(UUID sellerId, String type, String status, Pageable pageable);
    Flux<TransactionEntity> findByIdIn(List<UUID> ids);
    Mono<Long> countBySellerId(UUID sellerId);
    Mono<Long> countBySellerIdAndType(UUID sellerId, String type);
    Mono<Long> countBySellerIdAndStatus(UUID sellerId, String status);

    @Query("SELECT COUNT(*) FROM transactions WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Mono<Long> countBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);

    Mono<Long> countByStatus(String status);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE seller_id = :sellerId")
    Mono<Double> sumAmountBySellerId(UUID sellerId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE seller_id = :sellerId AND type = :type")
    Mono<Double> sumAmountBySellerIdAndType(UUID sellerId, String type);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Mono<Double> sumAmountBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(commission), 0) FROM transactions WHERE seller_id = :sellerId")
    Mono<Double> sumCommissionBySellerId(UUID sellerId);

    @Query("SELECT COALESCE(SUM(commission), 0) FROM transactions WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Mono<Double> sumCommissionBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE seller_id = :sellerId AND type = :type")
    Mono<Double> sumRefundsBySellerId(UUID sellerId, String type);

    @Query("SELECT COALESCE(SUM(net_amount), 0) FROM transactions WHERE seller_id = :sellerId")
    Mono<Double> sumNetAmountBySellerId(UUID sellerId);

    @Query("SELECT COALESCE(SUM(net_amount), 0) FROM transactions WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Mono<Double> sumNetAmountBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
