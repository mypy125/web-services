package com.mygitgor.transaction_service.infrastructure.persistence.entity;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@With
@Table("seller_settlements")
public record SettlementEntity (
        @Id UUID id,

        @Column("seller_id")
        UUID sellerId,

        String type,
        String status,
        @Column("reference_number")
        String referenceNumber,
        String description,

        @Column("total_amount")
        BigDecimal totalAmount,
        @Column("total_commission")
        BigDecimal totalCommission,
        @Column("total_tax")
        BigDecimal totalTax,
        @Column("total_shipping")
        BigDecimal totalShipping,
        @Column("total_discount")
        BigDecimal totalDiscount,
        @Column("total_refunds")
        BigDecimal totalRefunds,
        @Column("net_amount")
        BigDecimal netAmount,
        @Column("adjustment_amount")
        BigDecimal adjustmentAmount,
        String currency,

        @Column("transaction_ids_json")
        String transactionIdsJson,
        @Column("payout_ids_json")
        String payoutIdsJson,
        @Column("refund_ids_json")
        String refundIdsJson,
        @Column("reconciliation_id")
        UUID reconciliationId,

        @Column("payment_method")
        String paymentMethod,
        @Column("bank_name")
        String bankName,
        @Column("account_number")
        String accountNumber,
        @Column("account_holder_name")
        String accountHolderName,
        @Column("ifsc_code")
        String ifscCode,
        @Column("upi_id")
        String upiId,

        @Column("payment_gateway")
        String paymentGateway,
        @Column("gateway_transaction_id")
        String gatewayTransactionId,
        @Column("gateway_response")
        String gatewayResponse,
        @Column("gateway_reference")
        String gatewayReference,

        @Column("created_at")
        LocalDateTime createdAt,
        @Column("updated_at")
        LocalDateTime updatedAt,
        @Column("processed_at")
        LocalDateTime processedAt,
        @Column("completed_at")
        LocalDateTime completedAt,
        @Column("failed_at")
        LocalDateTime failedAt,
        @Column("settled_at")
        LocalDateTime settledAt,

        @Column("notes_json")
        String notesJson,
        @Column("processed_by")
        String processedBy,
        @Column("failure_reason")
        String failureReason,
        @Column("metadata")
        String metadataJson
) {
}
