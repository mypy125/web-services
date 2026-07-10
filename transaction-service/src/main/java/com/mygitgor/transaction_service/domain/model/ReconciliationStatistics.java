package com.mygitgor.transaction_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReconciliationStatistics {
    private final Long totalReconciliations;
    private final Long reconciledCount;
    private final Long pendingCount;
    private final Long rejectedCount;
    private final Long cancelledCount;
    private final BigDecimal totalNetSettlement;
    private final BigDecimal totalDiscrepancyAmount;
    private final Double averageReconciliationTimeHours;
    private final LocalDateTime calculatedAt;
}
