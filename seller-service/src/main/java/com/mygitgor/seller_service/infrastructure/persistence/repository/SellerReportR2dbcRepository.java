package com.mygitgor.seller_service.infrastructure.persistence.repository;

import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerReportEntity;
import com.mygitgor.seller_service.infrastructure.persistence.projection.PeriodSummaryProjection;
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
public interface SellerReportR2dbcRepository extends ReactiveCrudRepository<SellerReportEntity, UUID> {
    Flux<SellerReportEntity> findAllBySellerIdAndPeriod(UUID sellerId, String period);

    @Query("SELECT * FROM seller_reports WHERE seller_id = :sellerId AND period = :period ORDER BY report_generated_at DESC LIMIT 1")
    Mono<SellerReportEntity> findLatestReport(UUID sellerId, String period);

    Flux<SellerReportEntity> findBySellerId(UUID sellerId, Pageable pageable);

    @Query("SELECT * FROM seller_reports WHERE seller_id = :sellerId AND period_type = :periodType " +
            "AND period_start = :startDate AND period_end = :endDate")
    Mono<SellerReportEntity> findBySellerIdAndPeriod(UUID sellerId, String periodType,
                                                     LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT * FROM seller_reports WHERE seller_id = :sellerId " +
            "AND period_start BETWEEN :startDate AND :endDate")
    Flux<SellerReportEntity> findBySellerIdAndPeriodStartBetween(UUID sellerId,
                                                                 LocalDateTime startDate,
                                                                 LocalDateTime endDate,
                                                                 Pageable pageable);

    @Query("SELECT * FROM seller_reports WHERE seller_id = :sellerId AND period_type = :periodType")
    Flux<SellerReportEntity> findBySellerIdAndPeriodType(UUID sellerId, String periodType, Pageable pageable);

    Mono<SellerReportEntity> findTopBySellerIdOrderByCreatedAtDesc(UUID sellerId);

    @Query("SELECT * FROM seller_reports WHERE seller_id = :sellerId AND period_type = :periodType " +
            "ORDER BY created_at DESC LIMIT 1")
    Mono<SellerReportEntity> findTopBySellerIdAndPeriodTypeOrderByCreatedAtDesc(UUID sellerId, String periodType);

    Flux<SellerReportEntity> findByIdIn(List<UUID> ids);

    @Query("SELECT * FROM seller_reports WHERE period_start BETWEEN :startDate AND :endDate")
    Flux<SellerReportEntity> findByPeriodStartBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT * FROM seller_reports WHERE seller_id = :sellerId AND " +
            "(LOWER(period) LIKE LOWER(CONCAT('%', :term, '%')))")
    Flux<SellerReportEntity> searchBySellerId(UUID sellerId, String term, Pageable pageable);

    Mono<Void> deleteBySellerId(UUID sellerId);

    @Query("DELETE FROM seller_reports WHERE seller_id = :sellerId AND period_type = :periodType " +
            "AND period_start BETWEEN :startDate AND :endDate")
    Mono<Void> deleteBySellerIdAndPeriodAndDateBetween(UUID sellerId, String periodType,
                                                       LocalDateTime startDate, LocalDateTime endDate);

    Mono<Long> countBySellerId(UUID sellerId);
    Mono<Long> countBySellerIdAndPeriodType(UUID sellerId, String periodType);

    @Query("SELECT COUNT(*) FROM seller_reports WHERE seller_id = :sellerId " +
            "AND period_start BETWEEN :startDate AND :endDate")
    Mono<Long> countBySellerIdAndPeriodStartBetween(UUID sellerId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(total_earnings), 0) FROM seller_reports WHERE seller_id = :sellerId")
    Mono<Double> sumTotalEarningsBySellerId(UUID sellerId);

    @Query("SELECT COALESCE(SUM(total_sales), 0) FROM seller_reports WHERE seller_id = :sellerId")
    Mono<Double> sumTotalSalesBySellerId(UUID sellerId);

    @Query("SELECT COALESCE(SUM(total_orders), 0) FROM seller_reports WHERE seller_id = :sellerId")
    Mono<Long> sumTotalOrdersBySellerId(UUID sellerId);

    @Query("SELECT COALESCE(SUM(total_earnings), 0) FROM seller_reports WHERE seller_id = :sellerId " +
            "AND period_start BETWEEN :startDate AND :endDate")
    Mono<Double> sumTotalEarningsBySellerIdAndPeriodStartBetween(UUID sellerId,
                                                                 LocalDateTime startDate,
                                                                 LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(total_sales), 0) FROM seller_reports WHERE seller_id = :sellerId " +
            "AND period_start BETWEEN :startDate AND :endDate")
    Mono<Double> sumTotalSalesBySellerIdAndPeriodStartBetween(UUID sellerId,
                                                              LocalDateTime startDate,
                                                              LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(total_orders), 0) FROM seller_reports WHERE seller_id = :sellerId " +
            "AND period_start BETWEEN :startDate AND :endDate")
    Mono<Long> sumTotalOrdersBySellerIdAndPeriodStartBetween(UUID sellerId,
                                                             LocalDateTime startDate,
                                                             LocalDateTime endDate);

    @Query("SELECT MIN(period_start) FROM seller_reports WHERE seller_id = :sellerId")
    Mono<LocalDateTime> findMinPeriodStartBySellerId(UUID sellerId);

    @Query("SELECT MAX(period_end) FROM seller_reports WHERE seller_id = :sellerId")
    Mono<LocalDateTime> findMaxPeriodEndBySellerId(UUID sellerId);

    @Query("SELECT period, period_start, period_end, " +
            "total_orders, total_sales, total_earnings, average_order_value, " +
            "total_transactions, total_commission " +
            "FROM seller_reports WHERE seller_id = :sellerId AND period_type = :periodType " +
            "ORDER BY period_start DESC")
    Flux<PeriodSummaryProjection> findPeriodSummaryBySellerIdAndPeriodType(UUID sellerId,
                                                                           String periodType,
                                                                           Pageable pageable);
}
