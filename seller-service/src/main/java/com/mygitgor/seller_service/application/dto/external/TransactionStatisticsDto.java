package com.mygitgor.seller_service.application.dto.external;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record TransactionStatisticsDto(
        Long totalTransactions,
        Long completedTransactions,
        Long pendingTransactions,
        Long failedTransactions,
        Long refundedTransactions,
        Long cancelledTransactions,
        Double totalAmount,
        Double totalCommission,
        Double totalRefunds,
        Double totalNetAmount,
        Double averageTransactionAmount,
        LocalDateTime calculatedAt
) {
    public TransactionStatisticsDto {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }
}
