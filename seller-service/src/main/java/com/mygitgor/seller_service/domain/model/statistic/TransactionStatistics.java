package com.mygitgor.seller_service.domain.model.statistic;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionStatistics {
    private Long totalTransactions;
    private Long completedTransactions;
    private Long pendingTransactions;
    private Long failedTransactions;
    private Long refundedTransactions;
    private Long cancelledTransactions;
    private Double totalAmount;
    private Double totalCommission;
    private Double totalNetAmount;
    private Double averageTransactionAmount;
    private LocalDateTime calculatedAt;
}
