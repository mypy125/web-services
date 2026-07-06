package com.mygitgor.transaction_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PayoutStatistics {
    private Long totalPayouts;
    private Long completedPayouts;
    private Long pendingPayouts;
    private Long failedPayouts;
    private Long cancelledPayouts;
    private Double totalAmount;
    private Double totalFees;
    private Double totalNetAmount;
    private Double averagePayoutAmount;
    private LocalDateTime calculatedAt;
}
