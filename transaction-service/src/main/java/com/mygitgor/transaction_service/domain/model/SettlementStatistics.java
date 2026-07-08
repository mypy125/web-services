package com.mygitgor.transaction_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SettlementStatistics {
    private Long totalSettlements;
    private Long completedSettlements;
    private Long settledSettlements;
    private Long pendingSettlements;
    private Long failedSettlements;
    private Long cancelledSettlements;
    private Double totalAmount;
    private Double totalNetAmount;
    private Double averageSettlementAmount;
    private Double averageSettlementTimeHours;
    private LocalDateTime calculatedAt;
}
