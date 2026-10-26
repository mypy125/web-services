package com.mygitgor.seller_service.infrastructure.persistence;

import com.mygitgor.seller_service.domain.model.PeriodSummary;
import com.mygitgor.seller_service.domain.model.ReportPeriod;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.domain.model.statistic.ReportStatistics;
import com.mygitgor.seller_service.domain.repository.SellerReportRepositoryPort;
import com.mygitgor.seller_service.infrastructure.persistence.repository.SellerReportR2dbcRepository;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerReportId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SellerReportRepositoryAdapter implements SellerReportRepositoryPort {
    private final SellerReportR2dbcRepository reportR2dbcRepository;

    @Override
    public Mono<SellerReport> save(SellerReport report) {
        return null;
    }

    @Override
    public Mono<SellerReport> generateReport(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }

    @Override
    public Mono<Void> deleteById(SellerReportId reportId) {
        return null;
    }

    @Override
    public Mono<Void> deleteBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Void> deleteByPeriod(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }

    @Override
    public Mono<SellerReport> findById(SellerReportId reportId) {
        return null;
    }

    @Override
    public Flux<SellerReport> findBySellerId(SellerId sellerId, int page, int size) {
        return null;
    }

    @Override
    public Mono<SellerReport> findBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }

    @Override
    public Flux<SellerReport> findBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        return null;
    }

    @Override
    public Flux<SellerReport> findBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period, int page, int size) {
        return null;
    }

    @Override
    public Mono<SellerReport> findLatestBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<SellerReport> findLatestBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period) {
        return null;
    }

    @Override
    public Flux<SellerReport> findByIds(List<SellerReportId> reportIds) {
        return null;
    }

    @Override
    public Flux<SellerReport> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        return null;
    }

    @Override
    public Mono<Long> countBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Long> countBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period) {
        return null;
    }

    @Override
    public Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }

    @Override
    public Flux<SellerReport> search(SellerId sellerId, String searchTerm, int page, int size) {
        return null;
    }

    @Override
    public Flux<SellerReport> searchByDateRange(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        return null;
    }

    @Override
    public Mono<ReportStatistics> getReportStatistics(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<ReportStatistics> getReportStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }

    @Override
    public Flux<PeriodSummary> getPeriodSummary(SellerId sellerId, ReportPeriod period, int limit) {
        return null;
    }
}
