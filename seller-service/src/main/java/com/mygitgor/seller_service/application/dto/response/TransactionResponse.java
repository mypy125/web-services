package com.mygitgor.seller_service.application.dto.response;

import lombok.Builder;
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
            Double amount,
            Double tax,
            Double commission,
            Double shippingCost,
            Double discount,
            Double netAmount
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
