package com.mygitgor.seller_service.presentation.controller;

import com.mygitgor.seller_service.application.dto.response.SellerReportResponse;
import com.mygitgor.seller_service.application.service.SellerReportService;
import com.mygitgor.seller_service.domain.model.PeriodSummary;
import com.mygitgor.seller_service.shared.valueobject.ReportPeriod;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerReportId;
import com.mygitgor.seller_service.domain.model.statistic.ReportStatistics;
import com.mygitgor.seller_service.infrastructure.mapper.SellerReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/reports")
@RequiredArgsConstructor
public class SellerReportController {
    private final SellerReportService reportService;
    private final SellerReportMapper reportMapper;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate report for period")
    public Mono<SellerReportResponse> generateReport(@PathVariable String sellerId,
                                                     @RequestParam ReportPeriod period,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Generating report for seller: {}, period: {}", sellerId, period);
        return reportService.generateReport(new SellerId(sellerId), period, startDate, endDate)
                .map(reportMapper::toResponse);
    }

    @PostMapping("/generate/monthly")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate monthly report")
    public Mono<SellerReportResponse> generateMonthlyReport(@PathVariable String sellerId,
                                                            @RequestParam int year,
                                                            @RequestParam int month
    ) {
        log.info("Generating monthly report for seller: {}, year: {}, month: {}", sellerId, year, month);
        return reportService.generateMonthlyReport(new SellerId(sellerId), year, month)
                .map(reportMapper::toResponse);
    }

    @PostMapping("/generate/weekly")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate weekly report")
    public Mono<SellerReportResponse> generateWeeklyReport(@PathVariable String sellerId,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime weekStart
    ) {
        log.info("Generating weekly report for seller: {}, week start: {}", sellerId, weekStart);
        return reportService.generateWeeklyReport(new SellerId(sellerId), weekStart)
                .map(reportMapper::toResponse);
    }

    @PostMapping("/generate/daily")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate daily report")
    public Mono<SellerReportResponse> generateDailyReport(@PathVariable String sellerId) {
        log.info("Generating daily report for seller: {}", sellerId);
        return reportService.generateDailyReport(new SellerId(sellerId))
                .map(reportMapper::toResponse);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get report by ID")
    public Mono<SellerReportResponse> getReportById(@PathVariable String sellerId,
                                                    @PathVariable String reportId
    ) {
        log.info("Getting report by ID: {}", reportId);
        return reportService.getReportById(new SellerReportId(reportId))
                .map(reportMapper::toResponse);
    }

    @GetMapping
    @Operation(summary = "Get all reports for seller")
    public Flux<SellerReportResponse> getReportsBySellerId(@PathVariable String sellerId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Getting reports for seller: {}, page: {}, size: {}", sellerId, page, size);
        return reportService.getReportsBySellerId(new SellerId(sellerId), page, size)
                .map(reportMapper::toResponse);
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest report")
    public Mono<SellerReportResponse> getLatestReport(@PathVariable String sellerId) {
        log.info("Getting latest report for seller: {}", sellerId);
        return reportService.getLatestReport(new SellerId(sellerId))
                .map(reportMapper::toResponse);
    }

    @GetMapping("/latest/monthly")
    @Operation(summary = "Get latest monthly report")
    public Mono<SellerReportResponse> getLatestMonthlyReport(@PathVariable String sellerId) {
        log.info("Getting latest monthly report for seller: {}", sellerId);
        return reportService.getLatestMonthlyReport(new SellerId(sellerId))
                .map(reportMapper::toResponse);
    }

    @GetMapping("/by-period")
    @Operation(summary = "Get report by period")
    public Mono<SellerReportResponse> getReportByPeriod(@PathVariable String sellerId,
                                                        @RequestParam ReportPeriod period,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Getting report by period for seller: {}", sellerId);
        return reportService.getReportByPeriod(new SellerId(sellerId), period, startDate, endDate)
                .map(reportMapper::toResponse);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get report statistics")
    public Mono<ReportStatistics> getReportStatistics(@PathVariable String sellerId) {
        log.info("Getting report statistics for seller: {}", sellerId);
        return reportService.getReportStatistics(new SellerId(sellerId));
    }

    @GetMapping("/statistics/by-period")
    @Operation(summary = "Get report statistics by period")
    public Mono<ReportStatistics> getReportStatistics(@PathVariable String sellerId,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Getting report statistics for seller: {}, from: {} to: {}", sellerId, startDate, endDate);
        return reportService.getReportStatistics(new SellerId(sellerId), startDate, endDate);
    }

    @GetMapping("/period-summary")
    @Operation(summary = "Get period summary")
    public Flux<PeriodSummary> getPeriodSummary(@PathVariable String sellerId,
                                                @RequestParam ReportPeriod period,
                                                @RequestParam(defaultValue = "12") int limit
    ) {
        log.info("Getting period summary for seller: {}, period: {}, limit: {}", sellerId, period, limit);
        return reportService.getPeriodSummary(new SellerId(sellerId), period, limit);
    }


    @DeleteMapping("/{reportId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete report")
    public Mono<Void> deleteReport(@PathVariable String sellerId,
                                   @PathVariable String reportId
    ) {
        log.info("Deleting report: {}", reportId);
        return reportService.deleteReport(new SellerReportId(reportId));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete all reports for seller")
    public Mono<Void> deleteAllReports(@PathVariable String sellerId) {
        log.info("Deleting all reports for seller: {}", sellerId);
        return reportService.deleteAllReportsBySellerId(new SellerId(sellerId));
    }

    @DeleteMapping("/by-period")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete reports by period")
    public Mono<Void> deleteReportsByPeriod(@PathVariable String sellerId,
                                            @RequestParam ReportPeriod period,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Deleting reports for seller: {}, period: {}", sellerId, period);
        return reportService.deleteReportsByPeriod(new SellerId(sellerId), period, startDate, endDate);
    }

    @GetMapping("/search")
    @Operation(summary = "Search reports")
    public Flux<SellerReportResponse> searchReports(@PathVariable String sellerId,
                                                    @RequestParam String term,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Searching reports for seller: {}, term: {}", sellerId, term);
        return reportService.searchReports(new SellerId(sellerId), term, page, size)
                .map(reportMapper::toResponse);
    }

    @GetMapping("/search/by-date")
    @Operation(summary = "Search reports by date range")
    public Flux<SellerReportResponse> searchReportsByDateRange(@PathVariable String sellerId,
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Searching reports by date range for seller: {}", sellerId);
        return reportService.searchReportsByDateRange(new SellerId(sellerId), startDate, endDate, page, size)
                .map(reportMapper::toResponse);
    }
}
