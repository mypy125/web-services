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
@Table("seller_payouts")
public record PayoutEntity (
        @Id UUID id,

        @Column("seller_id")
        UUID sellerId,

        String type,
        String status,
        BigDecimal amount,
        BigDecimal fee,
        @Column("net_amount")
        BigDecimal netAmount,
        String currency,
        String description,
        @Column("reference_number")
        String referenceNumber,

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

        @Column("transaction_ids_json")
        String transactionIdsJson,
        @Column("settlement_id")
        UUID settlementId,

        @Column("payment_gateway")
        String paymentGateway,
        @Column("gateway_transaction_id")
        String gatewayTransactionId,
        @Column("gateway_response")
        String gatewayResponse,

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

        String notes,
        @Column("processed_by")
        String processedBy,
        @Column("failure_reason")
        String failureReason,
        @Column("metadata")
        String metadataJson
){
}
