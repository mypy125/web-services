package com.mygitgor.seller_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PeriodSummary {
    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long totalOrders;
    private Double totalSales;
    private Double totalEarnings;
    private Double averageOrderValue;
    private Integer totalTransactions;
    private Double totalCommission;
}
