package com.mygitgor.seller_service.domain.repository;

import com.mygitgor.seller_service.domain.model.PeriodSummary;
import com.mygitgor.seller_service.domain.model.statistic.ReportStatistics;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.domain.model.ReportPeriod;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerReportId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface SellerReportRepositoryPort {
    Mono<SellerReport> save(SellerReport report);

    Mono<SellerReport> generateReport(
            SellerId sellerId,
            ReportPeriod period,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Mono<Void> deleteById(SellerReportId reportId);
    Mono<Void> deleteBySellerId(SellerId sellerId);
    Mono<Void> deleteByPeriod(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate);
    Mono<SellerReport> findById(SellerReportId reportId);
    Flux<SellerReport> findBySellerId(SellerId sellerId, int page, int size);
    Mono<SellerReport> findBySellerIdAndPeriod(
            SellerId sellerId,
            ReportPeriod period,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Flux<SellerReport> findBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );
    Flux<SellerReport> findBySellerIdAndPeriod(
            SellerId sellerId,
            ReportPeriod period,
            int page,
            int size
    );

    Mono<SellerReport> findLatestBySellerId(SellerId sellerId);
    Mono<SellerReport> findLatestBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period);
    Flux<SellerReport> findByIds(List<SellerReportId> reportIds);
    Flux<SellerReport> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate, int page, int size);
    Mono<Long> countBySellerId(SellerId sellerId);
    Mono<Long> countBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period);
    Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Flux<SellerReport> search(SellerId sellerId, String searchTerm, int page, int size);
    Flux<SellerReport> searchByDateRange(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );
    Mono<ReportStatistics> getReportStatistics(SellerId sellerId);
    Mono<ReportStatistics> getReportStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Flux<PeriodSummary> getPeriodSummary(SellerId sellerId, ReportPeriod period, int limit);
}
