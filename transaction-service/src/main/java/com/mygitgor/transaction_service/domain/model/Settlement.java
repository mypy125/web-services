package com.mygitgor.transaction_service.domain.model;

import com.mygitgor.transaction_service.domain.model.valueobject.SettlementStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.SettlementType;
import com.mygitgor.transaction_service.domain.model.valueobject.TransactionType;
import com.mygitgor.transaction_service.shared.exception.DomainException;
import com.mygitgor.transaction_service.shared.valueobject.PayoutId;
import com.mygitgor.transaction_service.shared.valueobject.ReconciliationId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.SettlementId;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Builder
public class Settlement {
    private final SettlementId settlementId;
    private final SellerId sellerId;

    // TODO: Settlement Details
    private final SettlementType type;
    private SettlementStatus status;
    private final String referenceNumber;
    private String description;

    // TODO: Financial Summary
    private BigDecimal totalAmount;
    private BigDecimal totalCommission;
    private BigDecimal totalTax;
    private BigDecimal totalShipping;
    private BigDecimal totalDiscount;
    private BigDecimal totalRefunds;
    private BigDecimal netAmount;
    private BigDecimal adjustmentAmount;
    private final String currency;

    // TODO: Transaction References
    private Set<String> transactionIds;
    private Set<String> payoutIds;
    private Set<String> refundIds;
    private String reconciliationId;

    // TODO: Payment Details
    private String paymentMethod;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private String ifscCode;
    private String upiId;

    // TODO: Gateway Details
    private String paymentGateway;
    private String gatewayTransactionId;
    private String gatewayResponse;
    private String gatewayReference;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private LocalDateTime settledAt;

    private List<String> notes;
    private String processedBy;
    private String failureReason;
    private Map<String, String> metadata;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private Settlement(SettlementId settlementId, SellerId sellerId, SettlementType type,
                       SettlementStatus status, String referenceNumber, String description,
                       String currency, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.settlementId = Objects.requireNonNull(settlementId, "SettlementId cannot be null");
        this.sellerId = Objects.requireNonNull(sellerId, "SellerId cannot be null");
        this.type = Objects.requireNonNull(type, "SettlementType cannot be null");
        this.status = Objects.requireNonNull(status, "SettlementStatus cannot be null");
        this.referenceNumber = referenceNumber;
        this.description = description;
        this.currency = currency != null ? currency : "USD";
        this.totalAmount = BigDecimal.ZERO;
        this.totalCommission = BigDecimal.ZERO;
        this.totalTax = BigDecimal.ZERO;
        this.totalShipping = BigDecimal.ZERO;
        this.totalDiscount = BigDecimal.ZERO;
        this.totalRefunds = BigDecimal.ZERO;
        this.netAmount = BigDecimal.ZERO;
        this.adjustmentAmount = BigDecimal.ZERO;

        this.transactionIds = new LinkedHashSet<>();
        this.payoutIds = new LinkedHashSet<>();
        this.refundIds = new LinkedHashSet<>();
        this.notes = new ArrayList<>();
        this.metadata = new HashMap<>();

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Settlement create(SellerId sellerId,
                                    SettlementType type,
                                    String description
    ) {
        LocalDateTime now = LocalDateTime.now();
        return Settlement.builder()
                .settlementId(new SettlementId())
                .sellerId(sellerId)
                .type(type)
                .status(SettlementStatus.PENDING)
                .referenceNumber(generateReferenceNumber())
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Settlement createRegular(SellerId sellerId, SettlementType type, List<String> transactionIds) {
        Settlement settlement = create(sellerId, type, type.getDisplayName() + " settlement for seller");
        if (transactionIds != null) {
            settlement.transactionIds.addAll(transactionIds);
        }
        return settlement;
    }

    public static Settlement createManual(SellerId sellerId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Manual settlement amount must be positive");
        }
        Settlement settlement = create(sellerId, SettlementType.MANUAL, description != null ? description : "Manual settlement");
        settlement.totalAmount = amount;
        settlement.netAmount = amount;
        return settlement;
    }

    public void addTransaction(BigDecimal amount, BigDecimal commission,
                               BigDecimal shipping, BigDecimal discount,
                               BigDecimal tax, String txnId, boolean isRefund
    ) {
        ensureActiveState("add transaction");

        this.totalAmount = this.totalAmount.add(amount != null ? amount : BigDecimal.ZERO);
        this.totalCommission = this.totalCommission.add(commission != null ? commission : BigDecimal.ZERO);
        this.totalTax = this.totalTax.add(tax != null ? tax : BigDecimal.ZERO);
        this.totalShipping = this.totalShipping.add(shipping != null ? shipping : BigDecimal.ZERO);
        this.totalDiscount = this.totalDiscount.add(discount != null ? discount : BigDecimal.ZERO);

        if (isRefund) {
            this.totalRefunds = this.totalRefunds.add(amount != null ? amount : BigDecimal.ZERO);
            this.refundIds.add(txnId);
        } else {
            this.transactionIds.add(txnId);
        }

        this.netAmount = calculateNetAmount();
        this.updatedAt = LocalDateTime.now();
    }

    public void addPayout(PayoutId payoutId) {
        ensureActiveState("add payout link");
        if (payoutId != null) {
            this.payoutIds.add(payoutId.toString());
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void addAdjustment(BigDecimal amount, String reason) {
        ensureActiveState("add adjustment");
        BigDecimal val = amount != null ? amount : BigDecimal.ZERO;
        this.adjustmentAmount = this.adjustmentAmount.add(val);
        this.notes.add("Adjustment: " + reason + " (" + val + ")");
        this.netAmount = calculateNetAmount();
        this.updatedAt = LocalDateTime.now();
    }

    public void process(String processedBy) {
        if (this.status == SettlementStatus.PROCESSED) {
            throw new DomainException("Settlement already processed");
        }
        ensureActiveState("process");

        if (this.netAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Cannot process settlement with zero or negative net amount");
        }

        this.status = SettlementStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(String gatewayTransactionId, String gatewayResponse) {
        if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.SETTLED) {
            throw new DomainException("Settlement already closed");
        }
        if (this.status == SettlementStatus.FAILED) {
            throw new DomainException("Cannot complete failed settlement");
        }

        this.status = SettlementStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponse = gatewayResponse;
        this.updatedAt = LocalDateTime.now();
    }

    public void settle() {
        if (this.status == SettlementStatus.SETTLED) {
            throw new DomainException("Settlement already finalized as settled");
        }
        if (this.status != SettlementStatus.COMPLETED) {
            throw new DomainException("Settlement must be COMPLETED before switching to SETTLED status");
        }

        this.status = SettlementStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot fail a settlement that is already in terminal state");
        }

        this.status = SettlementStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot cancel a settlement that is already in terminal state");
        }

        this.status = SettlementStatus.CANCELLED;
        this.notes.add("Cancelled reason: " + reason);
        this.updatedAt = LocalDateTime.now();
    }

    public void recalculate() {
        this.netAmount = calculateNetAmount();
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePaymentDetails(String paymentMethod,
                                     String bankName, String accountNumber,
                                     String accountHolderName, String ifscCode
    ) {
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot update banking data on terminal settlement entity");
        }
        if (paymentMethod != null) this.paymentMethod = paymentMethod;
        if (bankName != null) this.bankName = bankName;
        if (accountNumber != null) this.accountNumber = accountNumber;
        if (accountHolderName != null) this.accountHolderName = accountHolderName;
        if (ifscCode != null) this.ifscCode = ifscCode;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateGatewayDetails(String gatewayReference, String gatewayTransactionId) {
        if (gatewayReference != null) this.gatewayReference = gatewayReference;
        if (gatewayTransactionId != null) this.gatewayTransactionId = gatewayTransactionId;
        this.updatedAt = LocalDateTime.now();
    }

    public void bindReconciliation(ReconciliationId recId) {
        if (recId != null) {
            this.reconciliationId = recId.toString();
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    public void addNote(String note) {
        if (note != null && !note.isBlank()) {
            this.notes.add(note);
            this.updatedAt = LocalDateTime.now();
        }
    }

    private BigDecimal calculateNetAmount() {
        return this.totalAmount
                .subtract(this.totalCommission)
                .subtract(this.totalTax)
                .subtract(this.totalShipping)
                .subtract(this.totalDiscount)
                .subtract(this.totalRefunds)
                .add(this.adjustmentAmount);
    }

    private void ensureActiveState(String operation) {
        if (this.status.isTerminal()) {
            throw new DomainException("Action '" + operation + "' denied. Settlement is in terminal state: " + this.status);
        }
    }

    private static String generateReferenceNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "SET-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }

    public Set<String> getTransactionIds() {
        return Collections.unmodifiableSet(transactionIds);
    }

    public Set<String> getPayoutIds() {
        return Collections.unmodifiableSet(payoutIds);
    }

    public Set<String> getRefundIds() {
        return Collections.unmodifiableSet(refundIds);
    }

    public List<String> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public boolean isPending() {
        return this.status == SettlementStatus.PENDING;
    }

    public boolean isProcessed() {
        return this.status == SettlementStatus.PROCESSED;
    }

    public boolean isCompleted() {
        return this.status == SettlementStatus.COMPLETED;
    }

    public boolean isSettled() {
        return this.status == SettlementStatus.SETTLED;
    }

    public boolean isFailed() {
        return this.status == SettlementStatus.FAILED;
    }

    public boolean isCancelled() {
        return this.status == SettlementStatus.CANCELLED;
    }

    public boolean isActive() {
        return this.status.isActive();
    }

    public boolean isTerminal() {
        return this.status.isTerminal();
    }

    public boolean canSettle() {
        return this.status == SettlementStatus.COMPLETED && this.settledAt == null;
    }

    public boolean isBalanced() {
        return this.netAmount.compareTo(BigDecimal.ZERO) == 0;
    }

    public int getTransactionCount() {
        return this.transactionIds.size();
    }

    public int getPayoutCount() {
        return this.payoutIds.size();
    }

    public int getRefundCount() {
        return this.refundIds.size();
    }

    @Override
    public String toString() {
        return String.format("Settlement{id=%s, sellerId=%s, status=%s, netAmount=%s, transactions=%d}",
                settlementId, sellerId, status, netAmount, getTransactionCount());
    }
}
