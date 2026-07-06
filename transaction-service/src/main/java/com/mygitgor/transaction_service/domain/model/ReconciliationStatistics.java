package com.mygitgor.transaction_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReconciliationStatistics {
    private Long totalReconciliations;
    private Long reconciledCount;
    private Long pendingCount;
    private Long rejectedCount;
    private Long cancelledCount;
    private Double totalNetSettlement;
    private Double totalDiscrepancyAmount;
    private Double averageReconciliationTimeHours;
    private LocalDateTime calculatedAt;
}
