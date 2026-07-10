package com.mygitgor.transaction_service.domain.model;

import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationType;
import com.mygitgor.transaction_service.shared.exception.DomainException;
import com.mygitgor.transaction_service.shared.valueobject.ReconciliationId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Builder
public class Reconciliation {
    private final ReconciliationId reconciliationId;
    private final SellerId sellerId;

    // TODO: Reconciliation Details
    private final ReconciliationType type;
    private ReconciliationStatus status;
    private final String period;
    private final LocalDateTime periodStart;
    private final LocalDateTime periodEnd;

    // TODO: Financial Summaries
    private BigDecimal totalSales;
    private BigDecimal totalRefunds;
    private BigDecimal totalCommission;
    private BigDecimal totalShipping;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal totalPayouts;
    private BigDecimal totalAdjustments;
    private BigDecimal netSettlement;

    // TODO: Transaction Counts
    private Integer totalTransactions;
    private Integer salesCount;
    private Integer refundsCount;
    private Integer payoutsCount;
    private Integer adjustmentsCount;

    // TODO: Discrepancies
    private BigDecimal discrepancyAmount;
    private String discrepancyReason;
    private Integer discrepancyCount;
    private Map<String, BigDecimal> discrepanciesByType;

    // TODO: Bank Pg Data
    private String bankStatementReference;
    private String paymentGatewayReference;
    private BigDecimal bankTotal;
    private BigDecimal gatewayTotal;
    private BigDecimal difference;

    // TODO: Resolution
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private List<String> resolutionActions;

    // TODO: Timestamp
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reconciledAt;

    // TODO: Metadata
    private List<String> notes;
    private String processedBy;
    private Map<String, String> metadata;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private Reconciliation(ReconciliationId reconciliationId,
                           SellerId sellerId, ReconciliationType type,
                           ReconciliationStatus status, String period,
                           LocalDateTime periodStart, LocalDateTime periodEnd,
                           LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.reconciliationId = Objects.requireNonNull(reconciliationId, "ReconciliationId cannot be null");
        this.sellerId = Objects.requireNonNull(sellerId, "SellerId cannot be null");
        this.type = Objects.requireNonNull(type, "ReconciliationType cannot be null");
        this.status = Objects.requireNonNull(status, "ReconciliationStatus cannot be null");
        this.period = period;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalSales = BigDecimal.ZERO;
        this.totalRefunds = BigDecimal.ZERO;
        this.totalCommission = BigDecimal.ZERO;
        this.totalShipping = BigDecimal.ZERO;
        this.totalDiscount = BigDecimal.ZERO;
        this.totalTax = BigDecimal.ZERO;
        this.totalPayouts = BigDecimal.ZERO;
        this.totalAdjustments = BigDecimal.ZERO;
        this.netSettlement = BigDecimal.ZERO;

        this.totalTransactions = 0;
        this.salesCount = 0;
        this.refundsCount = 0;
        this.payoutsCount = 0;
        this.adjustmentsCount = 0;

        this.discrepancyAmount = BigDecimal.ZERO;
        this.discrepancyCount = 0;
        this.discrepanciesByType = new HashMap<>();

        this.bankTotal = BigDecimal.ZERO;
        this.gatewayTotal = BigDecimal.ZERO;
        this.difference = BigDecimal.ZERO;

        this.resolutionActions = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.metadata = new HashMap<>();

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Reconciliation create(SellerId sellerId,
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
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Reconciliation createDaily(SellerId sellerId, LocalDateTime date) {
        LocalDateTime start = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        String period = date.toLocalDate().toString();
        return create(sellerId, ReconciliationType.DAILY, period, start, end);
    }

    public static Reconciliation createMonthly(SellerId sellerId, int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.withDayOfMonth(start.getMonth().length(start.toLocalDate().isLeapYear()))
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        String period = String.format("%d-%02d", year, month);
        return create(sellerId, ReconciliationType.MONTHLY, period, start, end);
    }

    public void addSale(BigDecimal amount,
                        BigDecimal commission,
                        BigDecimal shipping,
                        BigDecimal discount,
                        BigDecimal tax
    ) {
        ensureActiveState("add sale transaction");
        this.totalSales = this.totalSales.add(amount != null ? amount : BigDecimal.ZERO);
        this.totalCommission = this.totalCommission.add(commission != null ? commission : BigDecimal.ZERO);
        this.totalShipping = this.totalShipping.add(shipping != null ? shipping : BigDecimal.ZERO);
        this.totalDiscount = this.totalDiscount.add(discount != null ? discount : BigDecimal.ZERO);
        this.totalTax = this.totalTax.add(tax != null ? tax : BigDecimal.ZERO);

        this.salesCount++;
        this.totalTransactions++;
        calculateNetSettlement();
    }

    public void addRefund(BigDecimal amount) {
        ensureActiveState("add refund transaction");
        this.totalRefunds = this.totalRefunds.add(amount != null ? amount : BigDecimal.ZERO);
        this.refundsCount++;
        this.totalTransactions++;
        calculateNetSettlement();
    }

    public void addPayout(BigDecimal amount) {
        ensureActiveState("add payout transaction");
        this.totalPayouts = this.totalPayouts.add(amount != null ? amount : BigDecimal.ZERO);
        this.payoutsCount++;
        this.totalTransactions++;
        calculateNetSettlement();
    }

    public void addAdjustment(BigDecimal amount, String reason) {
        ensureActiveState("add adjustment transaction");
        this.totalAdjustments = this.totalAdjustments.add(amount != null ? amount : BigDecimal.ZERO);
        this.adjustmentsCount++;
        this.totalTransactions++;
        if (reason != null && !reason.isBlank()) {
            addDiscrepancy(reason, amount);
        }
        calculateNetSettlement();
    }

    public void addDiscrepancy(String errorType, BigDecimal amount) {
        ensureActiveState("add discrepancy log");
        BigDecimal val = amount != null ? amount : BigDecimal.ZERO;
        this.discrepanciesByType.put(errorType, this.discrepanciesByType.getOrDefault(errorType, BigDecimal.ZERO).add(val));
        this.discrepancyCount++;
        this.discrepancyAmount = this.discrepancyAmount.add(val);
        this.updatedAt = LocalDateTime.now();
    }

    public void addBankData(String bankStatementReference, BigDecimal bankTotal) {
        ensureActiveState("bind bank statement data");
        this.bankStatementReference = bankStatementReference;
        this.bankTotal = bankTotal != null ? bankTotal : BigDecimal.ZERO;
        calculateDifference();
    }

    public void addGatewayData(String paymentGatewayReference, BigDecimal gatewayTotal) {
        ensureActiveState("bind gateway reports data");
        this.paymentGatewayReference = paymentGatewayReference;
        this.gatewayTotal = gatewayTotal != null ? gatewayTotal : BigDecimal.ZERO;
        calculateDifference();
    }

    private void calculateNetSettlement() {
        this.netSettlement = this.totalSales
                .subtract(this.totalRefunds)
                .subtract(this.totalCommission)
                .subtract(this.totalShipping)
                .subtract(this.totalDiscount)
                .subtract(this.totalTax)
                .subtract(this.totalPayouts)
                .add(this.totalAdjustments);
        this.updatedAt = LocalDateTime.now();
    }

    private void calculateDifference() {
        this.difference = this.bankTotal.subtract(this.gatewayTotal);
        this.updatedAt = LocalDateTime.now();
    }

    public void start() {
        if (this.status != ReconciliationStatus.PENDING) {
            throw new DomainException("Reconciliation is not in PENDING state");
        }
        this.status = ReconciliationStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void reconcile(String resolvedBy) {
        if (this.status == ReconciliationStatus.RECONCILED) {
            throw new DomainException("Reconciliation is already finalized and reconciled");
        }
        if (this.status == ReconciliationStatus.CANCELLED) {
            throw new DomainException("Cannot finalize a cancelled reconciliation loop");
        }

        this.status = ReconciliationStatus.RECONCILED;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.reconciledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        if (this.status == ReconciliationStatus.RECONCILED) {
            throw new DomainException("Cannot reject an already verified and reconciled ledger");
        }

        this.status = ReconciliationStatus.REJECTED;
        this.discrepancyReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == ReconciliationStatus.RECONCILED) {
            throw new DomainException("Cannot cancel an already completed reconciliation");
        }

        this.status = ReconciliationStatus.CANCELLED;
        this.notes.add("Cancelled: " + reason);
        this.updatedAt = LocalDateTime.now();
    }

    public void addResolutionAction(String action) {
        if (action == null || action.isBlank()) return;
        this.resolutionActions.add(action);
        this.updatedAt = LocalDateTime.now();
    }

    public void addNote(String note) {
        if (note == null || note.isBlank()) return;
        this.notes.add(note);
        this.updatedAt = LocalDateTime.now();
    }

    public Map<String, BigDecimal> getDiscrepanciesByType() {
        return Collections.unmodifiableMap(discrepanciesByType);
    }

    public List<String> getResolutionActions() {
        return Collections.unmodifiableList(resolutionActions);
    }

    public List<String> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
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
        return this.discrepancyAmount.compareTo(BigDecimal.ZERO) != 0;
    }

    public boolean isBalanced() {
        return this.difference.compareTo(BigDecimal.ZERO) == 0;
    }

    public BigDecimal getTotalIncome() {
        return this.totalSales.add(this.totalAdjustments);
    }

    public BigDecimal getTotalExpenses() {
        return this.totalRefunds
                .add(this.totalCommission)
                .add(this.totalShipping)
                .add(this.totalDiscount)
                .add(this.totalTax)
                .add(this.totalPayouts);
    }

    private void ensureActiveState(String operation) {
        if (this.status.isTerminal()) {
            throw new DomainException("Forbidden: cannot execute operation '" + operation + "' on terminal ledger state: " + this.status);
        }
    }

    @Override
    public String toString() {
        return String.format("Reconciliation{id=%s, sellerId=%s, status=%s, period=%s, netSettlement=%s, discrepancy=%s}",
                reconciliationId, sellerId, status, period, netSettlement, discrepancyAmount);
    }
}