package com.mygitgor.transaction_service.infrastructure.kafka.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionRefundedEvent(
        String transactionId,
        String sellerId,
        String customerId,
        String orderId,
        BigDecimal amount,
        String currency,
        LocalDateTime refundedAt,
        LocalDateTime occurredAt
) {}