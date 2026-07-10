package com.mygitgor.transaction_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PayoutStatistics {
    private final Long totalPayouts;
    private final Long completedPayouts;
    private final Long pendingPayouts;
    private final Long failedPayouts;
    private final Long cancelledPayouts;
    private final BigDecimal totalAmount;
    private final BigDecimal totalFees;
    private final BigDecimal totalNetAmount;
    private final BigDecimal averagePayoutAmount;
    private final LocalDateTime calculatedAt;
}
