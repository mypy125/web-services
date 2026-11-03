package com.mygitgor.seller_service.infrastructure.persistence;

import com.mygitgor.seller_service.domain.model.PeriodSummary;
import com.mygitgor.seller_service.domain.model.ReportPeriod;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.domain.model.statistic.ReportStatistics;
import com.mygitgor.seller_service.domain.repository.SellerReportRepositoryPort;
import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerReportEntity;
import com.mygitgor.seller_service.infrastructure.persistence.mapper.SellerReportPersistenceMapper;
import com.mygitgor.seller_service.infrastructure.persistence.repository.SellerReportR2dbcRepository;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerReportId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Slf4j
@Repository
@RequiredArgsConstructor
public class SellerReportRepositoryAdapter implements SellerReportRepositoryPort {
    private final SellerReportR2dbcRepository repository;
    private final SellerReportPersistenceMapper mapper;

    @Override
    public Mono<SellerReport> save(SellerReport report) {
        log.debug("Saving report: {}", report.getReportId());
        SellerReportEntity entity = mapper.toEntity(report);
        return repository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(r -> log.debug("Report saved: {}", r.getReportId()))
                .doOnError(e -> log.error("Failed to save report: {}", e.getMessage()));
    }

    @Override
    public Mono<SellerReport> generateReport(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating report for seller: {}, period: {}", sellerId, period);
        return repository.findBySellerIdAndPeriod(
                        UUID.fromString(sellerId.toString()),
                        period.name(),
                        startDate,
                        endDate)
                .map(mapper::toDomain)
                .switchIfEmpty(Mono.defer(() -> {
                    SellerReport newReport = SellerReport.createNew(sellerId, period, startDate, endDate);
                    return save(newReport);
                }));
    }

    @Override
    public Mono<Void> deleteById(SellerReportId reportId) {
        log.debug("Deleting report: {}", reportId);
        return repository.deleteById(UUID.fromString(reportId.toString()));
    }

    @Override
    public Mono<Void> deleteBySellerId(SellerId sellerId) {
        log.debug("Deleting reports for seller: {}", sellerId);
        return repository.deleteBySellerId(UUID.fromString(sellerId.toString()));
    }

    @Override
    public Mono<Void> deleteByPeriod(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Deleting reports for seller: {}, period: {}", sellerId, period);
        return repository.deleteBySellerIdAndPeriodAndDateBetween(
                UUID.fromString(sellerId.toString()),
                period.name(),
                startDate,
                endDate);
    }

    @Override
    public Mono<SellerReport> findById(SellerReportId reportId) {
        log.debug("Finding report by ID: {}", reportId);
        return repository.findById(UUID.fromString(reportId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<SellerReport> findBySellerId(SellerId sellerId, int page, int size) {
        log.debug("Finding reports for seller: {}, page: {}, size: {}", sellerId, page, size);
        return repository.findBySellerId(
                        UUID.fromString(sellerId.toString()),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<SellerReport> findBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding report for seller: {}, period: {}", sellerId, period);
        return repository.findBySellerIdAndPeriod(
                        UUID.fromString(sellerId.toString()),
                        period.name(),
                        startDate,
                        endDate)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<SellerReport> findBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        log.debug("Finding reports for seller: {} between {} and {}", sellerId, startDate, endDate);
        return repository.findBySellerIdAndPeriodStartBetween(
                        UUID.fromString(sellerId.toString()),
                        startDate,
                        endDate,
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<SellerReport> findBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period, int page, int size) {
        log.debug("Finding reports for seller: {}, period: {}", sellerId, period);
        return repository.findBySellerIdAndPeriodType(
                        UUID.fromString(sellerId.toString()),
                        period.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<SellerReport> findLatestBySellerId(SellerId sellerId) {
        log.debug("Finding latest report for seller: {}", sellerId);
        return repository.findTopBySellerIdOrderByCreatedAtDesc(UUID.fromString(sellerId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<SellerReport> findLatestBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period) {
        log.debug("Finding latest report for seller: {}, period: {}", sellerId, period);
        return repository.findTopBySellerIdAndPeriodTypeOrderByCreatedAtDesc(
                        UUID.fromString(sellerId.toString()),
                        period.name())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<SellerReport> findByIds(List<SellerReportId> reportIds) {
        List<UUID> uuids = reportIds.stream()
                .map(id -> UUID.fromString(id.toString()))
                .toList();
        return repository.findByIdIn(uuids)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<SellerReport> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        log.debug("Finding reports between {} and {}", startDate, endDate);
        return repository.findByPeriodStartBetween(
                        startDate,
                        endDate,
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countBySellerId(SellerId sellerId) {
        log.debug("Counting reports for seller: {}", sellerId);
        return repository.countBySellerId(UUID.fromString(sellerId.toString()));
    }

    @Override
    public Mono<Long> countBySellerIdAndPeriod(SellerId sellerId, ReportPeriod period) {
        log.debug("Counting reports for seller: {}, period: {}", sellerId, period);
        return repository.countBySellerIdAndPeriodType(
                UUID.fromString(sellerId.toString()),
                period.name());
    }

    @Override
    public Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Counting reports for seller: {} between {} and {}", sellerId, startDate, endDate);
        return repository.countBySellerIdAndPeriodStartBetween(
                UUID.fromString(sellerId.toString()),
                startDate,
                endDate);
    }

    @Override
    public Flux<SellerReport> search(SellerId sellerId, String searchTerm, int page, int size) {
        log.debug("Searching reports for seller: {}, term: {}", sellerId, searchTerm);
        return repository.searchBySellerId(
                        UUID.fromString(sellerId.toString()),
                        searchTerm,
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<SellerReport> searchByDateRange(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        log.debug("Searching reports for seller: {} between {} and {}", sellerId, startDate, endDate);
        return repository.findBySellerIdAndPeriodStartBetween(
                        UUID.fromString(sellerId.toString()),
                        startDate,
                        endDate,
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<ReportStatistics> getReportStatistics(SellerId sellerId) {
        log.debug("Getting report statistics for seller: {}", sellerId);
        UUID sellerUuid = UUID.fromString(sellerId.toString());
        return Mono.zip(
                results -> {
                    Long total = (Long) results[0];
                    Long daily = (Long) results[1];
                    Long weekly = (Long) results[2];
                    Long monthly = (Long) results[3];
                    Long quarterly = (Long) results[4];
                    Long yearly = (Long) results[5];

                    BigDecimal totalEarningsBd = toBigDecimal(results[6]);
                    BigDecimal totalSalesBd = toBigDecimal(results[7]);
                    Long totalOrders = (Long) results[8];

                    LocalDateTime oldest = (LocalDateTime) results[9];
                    LocalDateTime newest = (LocalDateTime) results[10];

                    BigDecimal avgEarnings = (total != null && total > 0)
                            ? totalEarningsBd.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return ReportStatistics.builder()
                            .totalReports(total)
                            .dailyReports(daily)
                            .weeklyReports(weekly)
                            .monthlyReports(monthly)
                            .quarterlyReports(quarterly)
                            .yearlyReports(yearly)
                            .totalEarnings(totalEarningsBd.doubleValue())
                            .averageEarnings(avgEarnings.doubleValue())
                            .totalSales(totalSalesBd.doubleValue())
                            .totalOrders(totalOrders != null ? totalOrders.doubleValue() : 0.0)
                            .oldestReportDate(oldest)
                            .newestReportDate(newest)
                            .calculatedAt(LocalDateTime.now())
                            .build();
                },
                repository.countBySellerId(sellerUuid),
                repository.countBySellerIdAndPeriodType(sellerUuid, ReportPeriod.DAILY.name()),
                repository.countBySellerIdAndPeriodType(sellerUuid, ReportPeriod.WEEKLY.name()),
                repository.countBySellerIdAndPeriodType(sellerUuid, ReportPeriod.MONTHLY.name()),
                repository.countBySellerIdAndPeriodType(sellerUuid, ReportPeriod.QUARTERLY.name()),
                repository.countBySellerIdAndPeriodType(sellerUuid, ReportPeriod.YEARLY.name()),
                repository.sumTotalEarningsBySellerId(sellerUuid).defaultIfEmpty(0.0),
                repository.sumTotalSalesBySellerId(sellerUuid).defaultIfEmpty(0.0),
                repository.sumTotalOrdersBySellerId(sellerUuid).defaultIfEmpty(0L),
                repository.findMinPeriodStartBySellerId(sellerUuid),
                repository.findMaxPeriodEndBySellerId(sellerUuid)
        );
    }

    @Override
    public Mono<ReportStatistics> getReportStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting report statistics for seller: {} between {} and {}", sellerId, startDate, endDate);
        UUID sellerUuid = UUID.fromString(sellerId.toString());
        return Mono.zip(
                repository.countBySellerIdAndPeriodStartBetween(sellerUuid, startDate, endDate),
                repository.sumTotalEarningsBySellerIdAndPeriodStartBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0),
                repository.sumTotalSalesBySellerIdAndPeriodStartBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0),
                repository.sumTotalOrdersBySellerIdAndPeriodStartBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0L)
        ).map(tuple -> {
            Long total = tuple.getT1();
            BigDecimal totalEarningsBd = toBigDecimal(tuple.getT2());
            BigDecimal totalSalesBd = toBigDecimal(tuple.getT3());
            Long totalOrders = tuple.getT4();

            BigDecimal avgEarnings = total > 0
                    ? totalEarningsBd.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return ReportStatistics.builder()
                    .totalReports(total)
                    .totalEarnings(totalEarningsBd.doubleValue())
                    .averageEarnings(avgEarnings.doubleValue())
                    .totalSales(totalSalesBd.doubleValue())
                    .totalOrders(totalOrders.doubleValue())
                    .calculatedAt(LocalDateTime.now())
                    .build();
        });
    }

    @Override
    public Flux<PeriodSummary> getPeriodSummary(SellerId sellerId, ReportPeriod period, int limit) {
        log.debug("Getting period summary for seller: {}, period: {}, limit: {}", sellerId, period, limit);
        return repository.findPeriodSummaryBySellerIdAndPeriodType(
                        UUID.fromString(sellerId.toString()),
                        period.name(),
                        PageRequest.of(0, limit))
                .map(row -> PeriodSummary.builder()
                        .period(row.getPeriod())
                        .startDate(row.getPeriodStart())
                        .endDate(row.getPeriodEnd())
                        .totalOrders(row.getTotalOrders() != null ? row.getTotalOrders().longValue() : 0L) // Конвертируем Integer в Long, если в PeriodSummary ожидается Long
                        .totalSales(row.getTotalSales())
                        .totalEarnings(row.getTotalEarnings())
                        .averageOrderValue(row.getAverageOrderValue())
                        .totalTransactions(row.getTotalTransactions())
                        .totalCommission(row.getTotalCommission())
                        .build());
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
