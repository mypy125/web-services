package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.external.TransactionDto;
import com.mygitgor.seller_service.domain.model.OrderStats;
import com.mygitgor.seller_service.domain.model.PeriodSummary;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.domain.port.outgoing.TransactionPort;
import com.mygitgor.seller_service.shared.exception.SellerNotFoundException;
import com.mygitgor.seller_service.domain.model.ReportPeriod;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerReportId;
import com.mygitgor.seller_service.domain.model.statistic.ReportStatistics;
import com.mygitgor.seller_service.domain.port.outgoing.OrderPort;
import com.mygitgor.seller_service.domain.port.outgoing.ProductPort;
import com.mygitgor.seller_service.domain.repository.SellerReportRepositoryPort;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.infrastructure.kafka.producer.SellerEventProducer;
import com.mygitgor.seller_service.infrastructure.mapper.OrderStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerReportService {
    private final SellerReportRepositoryPort reportRepository;
    private final SellerRepositoryPort sellerRepository;
    private final TransactionPort transactionPort;
    private final SellerEventProducer eventProducer;
    private final OrderStatsMapper orderStatsMapper;
    private final OrderPort orderPort;
    private final ProductPort productPort;

    @Transactional
    public Mono<SellerReport> generateReport(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating report for seller: {}, period: {}, from: {} to: {}", sellerId, period, startDate, endDate);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> getOrCreateReport(sellerId, period, startDate, endDate))
                .flatMap(report -> transactionPort.getTransactionsBySellerIdAndDateBetween(
                                sellerId, startDate, endDate, 0, 5000)
                        .collectList()
                        .flatMap(transactions -> {
                            if (transactions.isEmpty()) {
                                log.warn("No transactions found in transaction-service for seller: {}", sellerId);
                                return Mono.just(report);
                            }

                            return Flux.fromIterable(transactions)
                                    .flatMap(transactionDto -> enrichTransactionData(transactionDto) // Передаем DTO
                                            .onErrorResume(e -> {
                                                log.warn("Fallback for transaction {}: {}", transactionDto.transactionId(), e.getMessage());
                                                return Mono.just(orderStatsMapper.toOrderStats(transactionDto)); // Используем обновленный маппер
                                            }))
                                    .collectList()
                                    .flatMap(orderStatsList -> {
                                        orderStatsList.forEach(orderStats -> {
                                            report.updateOrderStats(orderStats);
                                            report.updateOrderStatus(orderStats.status());
                                            report.updateCustomerStats(orderStats.isNewCustomer());
                                            report.updateProductStats(
                                                    orderStats.productId(),
                                                    orderStats.productName(),
                                                    orderStats.category(),
                                                    orderStats.quantity(),
                                                    orderStats.productTotal()
                                            );
                                        });
                                        report.calculateDerivedMetrics();
                                        return reportRepository.save(report);
                                    });
                        }))
                .delayUntil(eventProducer::sendSellerReportGeneratedEvent);
    }

    public Mono<SellerReport> generateMonthlyReport(SellerId sellerId, int year, int month) {
        log.info("Generating monthly report for seller: {}, year: {}, month: {}", sellerId, year, month);

        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        return generateReport(sellerId, ReportPeriod.MONTHLY, startDate, endDate);
    }

    public Mono<SellerReport> generateWeeklyReport(SellerId sellerId, LocalDateTime weekStart) {
        log.info("Generating weekly report for seller: {}, week start: {}", sellerId, weekStart);

        LocalDateTime startDate = weekStart.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endDate = startDate.plusWeeks(1).minusSeconds(1);

        return generateReport(sellerId, ReportPeriod.WEEKLY, startDate, endDate);
    }

    public Mono<SellerReport> generateYearlyReport(SellerId sellerId, int year) {
        log.info("Generating yearly report for seller: {}, year: {}", sellerId, year);

        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year + 1, 1, 1, 0, 0, 0).minusSeconds(1);

        return generateReport(sellerId, ReportPeriod.YEARLY, startDate, endDate);
    }

    public Mono<SellerReport> generateDailyReport(SellerId sellerId) {
        log.info("Generating daily report for seller: {}", sellerId);

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startDate = today.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endDate = today.withHour(23).withMinute(59).withSecond(59);

        return generateReport(sellerId, ReportPeriod.DAILY, startDate, endDate);
    }

    public Mono<SellerReport> getReportById(SellerReportId reportId) {
        log.debug("Getting report by ID: {}", reportId);
        return reportRepository.findById(reportId);
    }

    public Flux<SellerReport> getReportsBySellerId(SellerId sellerId, int page, int size) {
        log.debug("Getting reports for seller: {}, page: {}, size: {}", sellerId, page, size);
        return reportRepository.findBySellerId(sellerId, page, size);
    }

    public Mono<SellerReport> getReportByPeriod(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting report for seller: {}, period: {}, from: {} to: {}", sellerId, period, startDate, endDate);
        return reportRepository.findBySellerIdAndPeriod(sellerId, period, startDate, endDate);
    }

    public Mono<SellerReport> getLatestReport(SellerId sellerId) {
        log.debug("Getting latest report for seller: {}", sellerId);
        return reportRepository.findLatestBySellerId(sellerId);
    }

    public Mono<SellerReport> getLatestMonthlyReport(SellerId sellerId) {
        log.debug("Getting latest monthly report for seller: {}", sellerId);
        return reportRepository.findLatestBySellerIdAndPeriod(sellerId, ReportPeriod.MONTHLY);
    }

    public Mono<ReportStatistics> getReportStatistics(SellerId sellerId) {
        log.debug("Getting report statistics for seller: {}", sellerId);
        return reportRepository.getReportStatistics(sellerId);
    }

    public Mono<ReportStatistics> getReportStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting report statistics for seller: {}, from: {} to: {}", sellerId, startDate, endDate);
        return reportRepository.getReportStatistics(sellerId, startDate, endDate);
    }

    public Flux<PeriodSummary> getPeriodSummary(SellerId sellerId, ReportPeriod period, int limit) {
        log.debug("Getting period summary for seller: {}, period: {}, limit: {}", sellerId, period, limit);
        return reportRepository.getPeriodSummary(sellerId, period, limit);
    }

    @Transactional
    public Mono<Void> deleteReport(SellerReportId reportId) {
        log.info("Deleting report: {}", reportId);
        return reportRepository.deleteById(reportId);
    }

    @Transactional
    public Mono<Void> deleteAllReportsBySellerId(SellerId sellerId) {
        log.info("Deleting all reports for seller: {}", sellerId);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> reportRepository.deleteBySellerId(sellerId));
    }

    @Transactional
    public Mono<Void> deleteReportsByPeriod(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Deleting reports for seller: {}, period: {}, from: {} to: {}", sellerId, period, startDate, endDate);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> reportRepository.deleteByPeriod(sellerId, period, startDate, endDate));
    }

    public Flux<SellerReport> searchReports(SellerId sellerId, String searchTerm, int page, int size) {
        log.debug("Searching reports for seller: {}, term: {}", sellerId, searchTerm);
        return reportRepository.search(sellerId, searchTerm, page, size);
    }

    public Flux<SellerReport> searchReportsByDateRange(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        log.debug("Searching reports for seller: {}, from: {} to: {}", sellerId, startDate, endDate);
        return reportRepository.searchByDateRange(sellerId, startDate, endDate, page, size);
    }

    public Flux<SellerReport> generateReportsForAllSellers(ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating reports for all sellers, period: {}", period);

        return sellerRepository.findAll(0, Integer.MAX_VALUE)
                .flatMap(seller -> generateReport(seller.getSellerId(), period, startDate, endDate))
                .limitRate(10);
    }

    public Flux<SellerReport> generateReportsForActiveSellers(ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating reports for active sellers, period: {}", period);

        return sellerRepository.findActiveSellers(0, Integer.MAX_VALUE)
                .flatMap(seller -> generateReport(seller.getSellerId(), period, startDate, endDate));
    }

    private Mono<SellerReport> getOrCreateReport(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        return reportRepository.findBySellerIdAndPeriod(sellerId, period, startDate, endDate)
                .doOnNext(existing -> log.warn("Report already exists for seller: {}, period: {}. Overwriting stats.", sellerId, period))
                .switchIfEmpty(Mono.defer(() -> reportRepository.save(SellerReport.createNew(sellerId, period, startDate, endDate))));
    }

    private Mono<OrderStats> enrichTransactionData(TransactionDto transaction) {
        return orderPort.getOrderDetails(new OrderId(transaction.orderId()))
                .flatMap(orderDetails -> productPort.getProductDetails(new ProductId(orderDetails.productId()))
                        .map(productDetails -> orderStatsMapper.toOrderStatsFull(transaction, orderDetails, productDetails))
                );
    }
}