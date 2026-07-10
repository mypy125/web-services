package com.mygitgor.seller_service.application.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record TransactionResponse(
        String transactionId,
        String sellerId,
        String customerId,
        String orderId,
        String type,
        String status,
        String currency,
        String description,
        String referenceNumber,
        FinancialBreakdownResponse financialBreakdown,
        PaymentDetailsResponse paymentDetails,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime refundedAt,
        AuditMetadataResponse audit
) {

    @Builder
    public record FinancialBreakdownResponse(
            BigDecimal amount,
            BigDecimal tax,
            BigDecimal commission,
            BigDecimal shippingCost,
            BigDecimal discount,
            BigDecimal netAmount
    ) {}

    @Builder
    public record PaymentDetailsResponse(
            String paymentMethod,
            String paymentGateway,
            String bankReference
    ) {}

    @Builder
    public record AuditMetadataResponse(
            String notes,
            String processedBy,
            String ipAddress,
            String userAgent,
            Map<String, String> metadata
    ) {}
}
