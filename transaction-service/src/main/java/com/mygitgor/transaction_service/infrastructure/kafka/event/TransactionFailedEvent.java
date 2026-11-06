package com.mygitgor.transaction_service.infrastructure.kafka.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionFailedEvent(
        String transactionId,
        String sellerId,
        String orderId,
        BigDecimal amount,
        String reason,
        LocalDateTime failedAt,
        LocalDateTime occurredAt
) {}