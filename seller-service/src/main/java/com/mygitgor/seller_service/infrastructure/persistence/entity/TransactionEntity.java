package com.mygitgor.seller_service.infrastructure.persistence.entity;

import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("seller_transactions")
public record TransactionEntity(
        @Id UUID id,
        @Column("seller_id") UUID sellerId,
        @Column("customer_id") UUID customerId,
        @Column("order_id") UUID orderId,
        String type,
        String status,
        Double amount,
        Double tax,
        Double commission,
        @Column("shipping_cost") Double shippingCost,
        Double discount,
        @Column("net_amount") Double netAmount,
        String currency,
        String description,
        @Column("reference_number") String referenceNumber,
        @Column("payment_method") String paymentMethod,
        @Column("payment_gateway") String paymentGateway,
        @Column("bank_reference") String bankReference,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt,
        @Column("completed_at") LocalDateTime completedAt,
        @Column("refunded_at") LocalDateTime refundedAt,
        String notes,
        @Column("processed_by") String processedBy,
        @Column("ip_address") String ipAddress,
        @Column("user_agent") String userAgent,
        @Column("metadata") String metadataJson
) {}
