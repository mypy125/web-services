package com.mygitgor.seller_service.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PeriodSummary(
        String period,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long totalOrders,
        Double totalSales,
        Double totalEarnings,
        Double averageOrderValue,
        Integer totalTransactions,
        Double totalCommission
) {}
