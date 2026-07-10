package com.mygitgor.transaction_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionStatistics {
    private final Long totalTransactions;
    private final Long completedTransactions;
    private final Long pendingTransactions;
    private final Long failedTransactions;
    private final Long refundedTransactions;
    private final Long cancelledTransactions;
    private final BigDecimal totalAmount;
    private final BigDecimal totalCommission;
    private final BigDecimal totalRefunds;
    private final BigDecimal totalNetAmount;
    private final BigDecimal averageTransactionAmount;
    private final LocalDateTime calculatedAt;
}
