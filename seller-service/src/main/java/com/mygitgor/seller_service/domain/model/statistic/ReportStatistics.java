package com.mygitgor.seller_service.domain.model.statistic;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportStatistics {
    private Long totalReports;
    private Long dailyReports;
    private Long weeklyReports;
    private Long monthlyReports;
    private Long quarterlyReports;
    private Long yearlyReports;
    private Double totalEarnings;
    private Double averageEarnings;
    private Double totalSales;
    private Double totalOrders;
    private LocalDateTime oldestReportDate;
    private LocalDateTime newestReportDate;
    private LocalDateTime calculatedAt;
}
