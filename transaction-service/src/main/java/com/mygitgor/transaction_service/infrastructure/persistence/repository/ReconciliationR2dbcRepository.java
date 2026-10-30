package com.mygitgor.transaction_service.infrastructure.persistence.repository;

import com.mygitgor.transaction_service.infrastructure.persistence.entity.ReconciliationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReconciliationR2dbcRepository extends ReactiveCrudRepository<ReconciliationEntity, UUID> {
    Mono<ReconciliationEntity> findBySellerIdAndPeriod(UUID sellerId, String period);
    Flux<ReconciliationEntity> findBySellerId(UUID sellerId, Pageable pageable);
    Flux<ReconciliationEntity> findBySellerIdAndStatus(UUID sellerId, String status, Pageable pageable);
    Flux<ReconciliationEntity> findBySellerIdAndType(UUID sellerId, String type, Pageable pageable);
    Flux<ReconciliationEntity> findBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Flux<ReconciliationEntity> findByStatus(String status, Pageable pageable);
    Flux<ReconciliationEntity> findByIdIn(List<UUID> ids);
    Mono<Long> countBySellerId(UUID sellerId);
    Mono<Long> countBySellerIdAndStatus(UUID sellerId, String status);
    Mono<Long> countBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<Long> countByStatus(String status);

    @Query("SELECT SUM(net_settlement) FROM seller_reconciliations WHERE seller_id = :sellerId")
    Mono<Double> sumNetSettlementBySellerId(UUID sellerId);

    @Query("SELECT SUM(net_settlement) FROM seller_reconciliations WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Mono<Double> sumNetSettlementBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(discrepancy_amount) FROM seller_reconciliations WHERE seller_id = :sellerId")
    Mono<Double> sumDiscrepancyAmountBySellerId(UUID sellerId);

    @Query("SELECT SUM(discrepancy_amount) FROM seller_reconciliations WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Mono<Double> sumDiscrepancyAmountBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
