package com.mygitgor.transaction_service.infrastructure.kafka.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionCompletedEvent(
        String transactionId,
        String sellerId,
        String orderId,
        BigDecimal amount,
        BigDecimal netAmount,
        String currency,
        LocalDateTime completedAt,
        LocalDateTime occurredAt
) {}
