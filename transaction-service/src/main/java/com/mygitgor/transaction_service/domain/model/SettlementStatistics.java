package com.mygitgor.transaction_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SettlementStatistics {
    private final Long totalSettlements;
    private final Long completedSettlements;
    private final Long settledSettlements;
    private final Long pendingSettlements;
    private final Long failedSettlements;
    private final Long cancelledSettlements;
    private final BigDecimal totalAmount;
    private final BigDecimal totalNetAmount;
    private final BigDecimal averageSettlementAmount;
    private final Double averageSettlementTimeHours;
    private final LocalDateTime calculatedAt;
}
