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
@Table("seller_reconciliations")
public record ReconciliationEntity (
        @Id UUID id,

        @Column("seller_id")
        UUID sellerId,

        String type,
        String status,
        String period,
        @Column("period_start")
        LocalDateTime periodStart,
        @Column("period_end")
        LocalDateTime periodEnd,

        @Column("total_sales")
        BigDecimal totalSales,
        @Column("total_refunds")
        BigDecimal totalRefunds,
        @Column("total_commission")
        BigDecimal totalCommission,
        @Column("total_shipping")
        BigDecimal totalShipping,
        @Column("total_discount")
        BigDecimal totalDiscount,
        @Column("total_tax")
        BigDecimal totalTax,
        @Column("total_payouts")
        BigDecimal totalPayouts,
        @Column("total_adjustments")
        BigDecimal totalAdjustments,
        @Column("net_settlement")
        BigDecimal netSettlement,

        @Column("total_transactions")
        Integer totalTransactions,
        @Column("sales_count")
        Integer salesCount,
        @Column("refunds_count")
        Integer refundsCount,
        @Column("payouts_count")
        Integer payoutsCount,
        @Column("adjustments_count")
        Integer adjustmentsCount,

        @Column("discrepancy_amount")
        BigDecimal discrepancyAmount,
        @Column("discrepancy_reason")
        String discrepancyReason,
        @Column("discrepancy_count")
        Integer discrepancyCount,
        @Column("discrepancies_by_type_json")
        String discrepanciesByTypeJson,

        @Column("bank_statement_reference")
        String bankStatementReference,
        @Column("payment_gateway_reference")
        String paymentGatewayReference,
        @Column("bank_total")
        BigDecimal bankTotal,
        @Column("gateway_total")
        BigDecimal gatewayTotal,
        BigDecimal difference,

        @Column("resolved_by")
        String resolvedBy,
        @Column("resolved_at")
        LocalDateTime resolvedAt,
        @Column("resolution_notes")
        String resolutionNotes,
        @Column("resolution_actions_json")
        String resolutionActionsJson,

        @Column("created_at")
        LocalDateTime createdAt,
        @Column("updated_at")
        LocalDateTime updatedAt,
        @Column("reconciled_at")
        LocalDateTime reconciledAt,

        @Column("notes_json")
        String notesJson,
        @Column("processed_by")
        String processedBy,
        @Column("metadata")
        String metadataJson
) {
}
