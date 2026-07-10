package com.mygitgor.seller_service.application.dto.external;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record TransactionDto(
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
        String referenceNumber,
        String paymentMethod,
        String paymentGateway,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        LocalDateTime refundedAt,
        Map<String, String> metadata
) {
    public TransactionDto {
        if (metadata != null) {
            metadata = Map.copyOf(metadata);
        }
    }
}