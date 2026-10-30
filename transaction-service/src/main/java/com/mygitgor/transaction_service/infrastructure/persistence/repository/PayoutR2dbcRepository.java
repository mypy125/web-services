package com.mygitgor.transaction_service.infrastructure.persistence.repository;

import com.mygitgor.transaction_service.infrastructure.persistence.entity.PayoutEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PayoutR2dbcRepository extends ReactiveCrudRepository<PayoutEntity, UUID> {
    Mono<PayoutEntity> findByReferenceNumber(String referenceNumber);
    Flux<PayoutEntity> findBySellerId(UUID sellerId, Pageable pageable);
    Flux<PayoutEntity> findBySellerIdAndStatus(UUID sellerId, String status, Pageable pageable);
    Flux<PayoutEntity> findBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Flux<PayoutEntity> findByStatus(String status, Pageable pageable);
    Flux<PayoutEntity> findByIdIn(List<UUID> ids);
    Mono<Long> countBySellerId(UUID sellerId);
    Mono<Long> countBySellerIdAndStatus(UUID sellerId, String status);
    Mono<Long> countBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<Long> countByStatus(String status);

    @Query("SELECT SUM(amount) FROM seller_payouts WHERE seller_id = :sellerId")
    Mono<Double> sumAmountBySellerId(UUID sellerId);

    @Query("SELECT SUM(amount) FROM seller_payouts WHERE seller_id = :sellerId AND status = :status")
    Mono<Double> sumAmountBySellerIdAndStatus(UUID sellerId, String status);

    @Query("SELECT SUM(amount) FROM seller_payouts WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Mono<Double> sumAmountBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(fee) FROM seller_payouts WHERE seller_id = :sellerId")
    Mono<Double> sumFeeBySellerId(UUID sellerId);

    @Query("SELECT SUM(fee) FROM seller_payouts WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Mono<Double> sumFeeBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(net_amount) FROM seller_payouts WHERE seller_id = :sellerId")
    Mono<Double> sumNetAmountBySellerId(UUID sellerId);

    @Query("SELECT SUM(net_amount) FROM seller_payouts WHERE seller_id = :sellerId AND created_at BETWEEN :startDate AND :endDate")
    Mono<Double> sumNetAmountBySellerIdAndCreatedAtBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
