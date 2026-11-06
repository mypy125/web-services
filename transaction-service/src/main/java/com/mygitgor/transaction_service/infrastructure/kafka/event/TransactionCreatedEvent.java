package com.mygitgor.transaction_service.infrastructure.kafka.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionCreatedEvent(
        String transactionId,
        String sellerId,
        String customerId,
        String orderId,
        String type,
        String status,
        BigDecimal amount,
        BigDecimal tax,
        BigDecimal commission,
        BigDecimal shippingCost,
        BigDecimal discount,
        BigDecimal netAmount,
        String currency,
        String description,
        LocalDateTime occurredAt
) {}
