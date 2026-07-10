package com.mygitgor.seller_service.application.dto.external;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionStatisticsDto(
        Long totalTransactions,
        Long completedTransactions,
        Long pendingTransactions,
        Long failedTransactions,
        Long refundedTransactions,
        Long cancelledTransactions,
        BigDecimal totalAmount,
        BigDecimal totalCommission,
        BigDecimal totalRefunds,
        BigDecimal totalNetAmount,
        BigDecimal averageTransactionAmount,
        LocalDateTime calculatedAt
) {
    public TransactionStatisticsDto {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }
}
