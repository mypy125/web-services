package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.shared.valueobject.ReportPeriod;
import com.mygitgor.seller_service.domain.model.statistic.ReportStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class SellerAdminService {
    private final SellerReportService reportService;

    public Flux<SellerReport> generateReportsForAllSellers(ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Admin generating reports for all sellers");
        return reportService.generateReportsForAllSellers(period, startDate, endDate);
    }

    public Mono<ReportStatistics> getGlobalReportStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting global report statistics");
        return reportService.getReportStatistics(null, startDate, endDate);
    }
}
