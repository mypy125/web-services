package com.mygitgor.transaction_service.domain.model;

import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationType;
import com.mygitgor.transaction_service.shared.exception.DomainException;
import com.mygitgor.transaction_service.shared.valueobject.ReconciliationId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class Reconciliation {
    private final ReconciliationId reconciliationId;
    private final SellerId sellerId;

    // TODO: Reconciliation Details
    private ReconciliationType type;
    private ReconciliationStatus status;
    private String period;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // TODO: Financial Summaries
    private Double totalSales;
    private Double totalRefunds;
    private Double totalCommission;
    private Double totalShipping;
    private Double totalDiscount;
    private Double totalTax;
    private Double totalPayouts;
    private Double totalAdjustments;
    private Double netSettlement;

    // TODO: Transaction Counts
    private Integer totalTransactions;
    private Integer salesCount;
    private Integer refundsCount;
    private Integer payoutsCount;
    private Integer adjustmentsCount;

    // TODO: Discrepancies
    private Double discrepancyAmount;
    private String discrepancyReason;
    private Integer discrepancyCount;
    private Map<String, Double> discrepanciesByType;

    // TODO: Bank Pg Data
    private String bankStatementReference;
    private String paymentGatewayReference;
    private Double bankTotal;
    private Double gatewayTotal;
    private Double difference;

    // TODO: Resolution
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private List<String> resolutionActions;

    // TODO: Timestamp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reconciledAt;

    // TODO: Metadata
    private String notes;
    private String processedBy;
    private Map<String, String> metadata;

    public static Reconciliation create(
            SellerId sellerId,
            ReconciliationType type,
            String period,
            LocalDateTime periodStart,
            LocalDateTime periodEnd
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Reconciliation.builder()
                .reconciliationId(new ReconciliationId())
                .sellerId(sellerId)
                .type(type)
                .status(ReconciliationStatus.PENDING)
                .period(period)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .totalSales(0.0)
                .totalRefunds(0.0)
                .totalCommission(0.0)
                .totalShipping(0.0)
                .totalDiscount(0.0)
                .totalTax(0.0)
                .totalPayouts(0.0)
                .totalAdjustments(0.0)
                .netSettlement(0.0)
                .totalTransactions(0)
                .salesCount(0)
                .refundsCount(0)
                .payoutsCount(0)
                .adjustmentsCount(0)
                .discrepancyAmount(0.0)
                .discrepancyCount(0)
                .discrepanciesByType(new HashMap<>())
                .difference(0.0)
                .resolutionActions(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .metadata(new HashMap<>())
                .build();
    }

    public static Reconciliation createDaily(SellerId sellerId, LocalDateTime date) {
        LocalDateTime start = date.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = date.withHour(23).withMinute(59).withSecond(59);
        String period = date.toLocalDate().toString();

        return create(sellerId, ReconciliationType.DAILY, period, start, end);
    }

    public static Reconciliation createMonthly(SellerId sellerId, int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.withDayOfMonth(start.getMonth().length(start.toLocalDate().isLeapYear()))
                .withHour(23).withMinute(59).withSecond(59);
        String period = String.format("%d-%02d", year, month);

        return create(sellerId, ReconciliationType.MONTHLY, period, start, end);
    }

    public void addSale(Double amount, Double commission, Double shipping, Double discount, Double tax) {
        this.totalSales = (this.totalSales != null ? this.totalSales : 0.0) + amount;
        this.totalCommission = (this.totalCommission != null ? this.totalCommission : 0.0) + commission;
        this.totalShipping = (this.totalShipping != null ? this.totalShipping : 0.0) + shipping;
        this.totalDiscount = (this.totalDiscount != null ? this.totalDiscount : 0.0) + discount;
        this.totalTax = (this.totalTax != null ? this.totalTax : 0.0) + tax;
        this.salesCount = (this.salesCount != null ? this.salesCount : 0) + 1;
        this.totalTransactions = (this.totalTransactions != null ? this.totalTransactions : 0) + 1;
        this.updatedAt = LocalDateTime.now();
        calculateNetSettlement();
    }

    public void addRefund(Double amount) {
        this.totalRefunds = (this.totalRefunds != null ? this.totalRefunds : 0.0) + amount;
        this.refundsCount = (this.refundsCount != null ? this.refundsCount : 0) + 1;
        this.totalTransactions = (this.totalTransactions != null ? this.totalTransactions : 0) + 1;
        this.updatedAt = LocalDateTime.now();
        calculateNetSettlement();
    }

    public void addPayout(Double amount) {
        this.totalPayouts = (this.totalPayouts != null ? this.totalPayouts : 0.0) + amount;
        this.payoutsCount = (this.payoutsCount != null ? this.payoutsCount : 0) + 1;
        this.totalTransactions = (this.totalTransactions != null ? this.totalTransactions : 0) + 1;
        this.updatedAt = LocalDateTime.now();
        calculateNetSettlement();
    }

    public void addAdjustment(Double amount, String reason) {
        this.totalAdjustments = (this.totalAdjustments != null ? this.totalAdjustments : 0.0) + amount;
        this.adjustmentsCount = (this.adjustmentsCount != null ? this.adjustmentsCount : 0) + 1;
        this.totalTransactions = (this.totalTransactions != null ? this.totalTransactions : 0) + 1;
        if (reason != null) {
            addDiscrepancy(reason, amount);
        }
        this.updatedAt = LocalDateTime.now();
        calculateNetSettlement();
    }

    public void addDiscrepancy(String type, Double amount) {
        if (this.discrepanciesByType == null) {
            this.discrepanciesByType = new HashMap<>();
        }
        this.discrepanciesByType.put(type,
                this.discrepanciesByType.getOrDefault(type, 0.0) + amount);
        this.discrepancyCount = (this.discrepancyCount != null ? this.discrepancyCount : 0) + 1;
        this.discrepancyAmount = (this.discrepancyAmount != null ? this.discrepancyAmount : 0.0) + amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void addBankData(String bankStatementReference, Double bankTotal) {
        this.bankStatementReference = bankStatementReference;
        this.bankTotal = bankTotal;
        this.updatedAt = LocalDateTime.now();
        calculateDifference();
    }

    public void addGatewayData(String paymentGatewayReference, Double gatewayTotal) {
        this.paymentGatewayReference = paymentGatewayReference;
        this.gatewayTotal = gatewayTotal;
        this.updatedAt = LocalDateTime.now();
        calculateDifference();
    }

    private void calculateNetSettlement() {
        this.netSettlement = (this.totalSales != null ? this.totalSales : 0.0)
                - (this.totalRefunds != null ? this.totalRefunds : 0.0)
                - (this.totalCommission != null ? this.totalCommission : 0.0)
                - (this.totalShipping != null ? this.totalShipping : 0.0)
                - (this.totalDiscount != null ? this.totalDiscount : 0.0)
                - (this.totalTax != null ? this.totalTax : 0.0)
                - (this.totalPayouts != null ? this.totalPayouts : 0.0)
                + (this.totalAdjustments != null ? this.totalAdjustments : 0.0);
        this.updatedAt = LocalDateTime.now();
    }

    private void calculateDifference() {
        if (this.bankTotal != null && this.gatewayTotal != null) {
            this.difference = this.bankTotal - this.gatewayTotal;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void start() {
        if (this.status != ReconciliationStatus.PENDING) {
            throw new DomainException("Reconciliation is not in pending state");
        }
        this.status = ReconciliationStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void reconcile(String resolvedBy) {
        if (this.status == ReconciliationStatus.RECONCILED) {
            throw new DomainException("Reconciliation already reconciled");
        }
        if (this.status == ReconciliationStatus.CANCELLED) {
            throw new DomainException("Cannot reconcile cancelled reconciliation");
        }

        this.status = ReconciliationStatus.RECONCILED;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.reconciledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        if (this.status == ReconciliationStatus.RECONCILED) {
            throw new DomainException("Cannot reject reconciled reconciliation");
        }

        this.status = ReconciliationStatus.REJECTED;
        this.discrepancyReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == ReconciliationStatus.RECONCILED) {
            throw new DomainException("Cannot cancel reconciled reconciliation");
        }

        this.status = ReconciliationStatus.CANCELLED;
        this.notes = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void addResolutionAction(String action) {
        if (this.resolutionActions == null) {
            this.resolutionActions = new ArrayList<>();
        }
        this.resolutionActions.add(action);
        this.updatedAt = LocalDateTime.now();
    }

    public void addNote(String note) {
        if (this.notes == null) {
            this.notes = "";
        }
        this.notes += note + " | ";
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == ReconciliationStatus.PENDING;
    }

    public boolean isInProgress() {
        return this.status == ReconciliationStatus.IN_PROGRESS;
    }

    public boolean isReconciled() {
        return this.status == ReconciliationStatus.RECONCILED;
    }

    public boolean isRejected() {
        return this.status == ReconciliationStatus.REJECTED;
    }

    public boolean isCancelled() {
        return this.status == ReconciliationStatus.CANCELLED;
    }

    public boolean hasDiscrepancies() {
        return this.discrepancyAmount != null && this.discrepancyAmount != 0.0;
    }

    public boolean isBalanced() {
        return this.difference == null || this.difference == 0.0;
    }

    public Double getNetSettlement() {
        return this.netSettlement != null ? this.netSettlement : 0.0;
    }

    public Double getTotalIncome() {
        return (this.totalSales != null ? this.totalSales : 0.0)
                + (this.totalAdjustments != null ? this.totalAdjustments : 0.0);
    }

    public Double getTotalExpenses() {
        return (this.totalRefunds != null ? this.totalRefunds : 0.0)
                + (this.totalCommission != null ? this.totalCommission : 0.0)
                + (this.totalShipping != null ? this.totalShipping : 0.0)
                + (this.totalDiscount != null ? this.totalDiscount : 0.0)
                + (this.totalTax != null ? this.totalTax : 0.0)
                + (this.totalPayouts != null ? this.totalPayouts : 0.0);
    }

    @Override
    public String toString() {
        return String.format("Reconciliation{id=%s, sellerId=%s, status=%s, period=%s, netSettlement=%s, discrepancy=%s}",
                reconciliationId, sellerId, status, period, netSettlement, discrepancyAmount);
    }
}