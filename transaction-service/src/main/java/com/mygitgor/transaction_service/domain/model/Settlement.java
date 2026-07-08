package com.mygitgor.transaction_service.domain.model;

import com.mygitgor.transaction_service.domain.model.valueobject.SettlementStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.SettlementType;
import com.mygitgor.transaction_service.domain.model.valueobject.TransactionType;
import com.mygitgor.transaction_service.shared.exception.DomainException;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.SettlementId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class Settlement {
    private final SettlementId settlementId;
    private final SellerId sellerId;

    // TODO: Settlement Details
    private SettlementType type;
    private SettlementStatus status;
    private String referenceNumber;
    private String description;

    // TODO: Financial Summary
    private Double totalAmount;
    private Double totalCommission;
    private Double totalTax;
    private Double totalShipping;
    private Double totalDiscount;
    private Double totalRefunds;
    private Double netAmount;
    private Double adjustmentAmount;
    private String currency;

    // TODO: Transaction References
    private List<String> transactionIds;
    private List<String> payoutIds;
    private List<String> refundIds;
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private LocalDateTime settledAt;

    private String notes;
    private String processedBy;
    private String failureReason;
    private Map<String, String> metadata;


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
                .totalAmount(0.0)
                .totalCommission(0.0)
                .totalTax(0.0)
                .totalShipping(0.0)
                .totalDiscount(0.0)
                .totalRefunds(0.0)
                .netAmount(0.0)
                .adjustmentAmount(0.0)
                .currency("USD")
                .transactionIds(new ArrayList<>())
                .payoutIds(new ArrayList<>())
                .refundIds(new ArrayList<>())
                .metadata(new HashMap<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Settlement createRegular(SellerId sellerId,
                                           SettlementType type,
                                           List<String> transactionIds
    ) {
        Settlement settlement = create(
                sellerId,
                type,
                type.getDisplayName() + " settlement for seller"
        );

        if (transactionIds != null) {
            settlement.transactionIds = new ArrayList<>(transactionIds);
        }

        return settlement;
    }

    public static Settlement createManual(SellerId sellerId,
                                          Double amount,
                                          String description
    ) {
        Settlement settlement = create(
                sellerId,
                SettlementType.MANUAL,
                description != null ? description : "Manual settlement"
        );
        settlement.totalAmount = amount;
        settlement.netAmount = amount;

        return settlement;
    }

    public void addTransaction(Transaction transaction) {
        if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.SETTLED) {
            throw new DomainException("Cannot add transaction to completed/settled settlement");
        }

        this.totalAmount = (this.totalAmount != null ? this.totalAmount : 0.0) + transaction.getAmount();
        this.totalCommission = (this.totalCommission != null ? this.totalCommission : 0.0) + transaction.getCommission();
        this.totalTax = (this.totalTax != null ? this.totalTax : 0.0) + transaction.getTax();
        this.totalShipping = (this.totalShipping != null ? this.totalShipping : 0.0) + transaction.getShippingCost();
        this.totalDiscount = (this.totalDiscount != null ? this.totalDiscount : 0.0) + transaction.getDiscount();

        if (transaction.getType() == TransactionType.REFUND) {
            this.totalRefunds = (this.totalRefunds != null ? this.totalRefunds : 0.0) + transaction.getAmount();
            if (this.refundIds == null) this.refundIds = new ArrayList<>();
            this.refundIds.add(transaction.getTransactionId().toString());
        } else {
            if (this.transactionIds == null) this.transactionIds = new ArrayList<>();
            this.transactionIds.add(transaction.getTransactionId().toString());
        }

        this.netAmount = calculateNetAmount();
        this.updatedAt = LocalDateTime.now();
    }

    public void addPayout(Payout payout) {
        if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.SETTLED) {
            throw new DomainException("Cannot add payout to completed/settled settlement");
        }

        if (this.payoutIds == null) this.payoutIds = new ArrayList<>();
        this.payoutIds.add(payout.getPayoutId().toString());
        this.updatedAt = LocalDateTime.now();
    }

    public void addAdjustment(Double amount, String reason) {
        if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.SETTLED) {
            throw new DomainException("Cannot add adjustment to completed/settled settlement");
        }

        this.adjustmentAmount = (this.adjustmentAmount != null ? this.adjustmentAmount : 0.0) + amount;
        this.notes = (this.notes != null ? this.notes + " | " : "") + "Adjustment: " + reason + " (" + amount + ")";
        this.netAmount = calculateNetAmount();
        this.updatedAt = LocalDateTime.now();
    }

    public void process(String processedBy) {
        if (this.status == SettlementStatus.PROCESSED) {
            throw new DomainException("Settlement already processed");
        }
        if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.SETTLED) {
            throw new DomainException("Cannot process completed/settled settlement");
        }
        if (this.status == SettlementStatus.FAILED) {
            throw new DomainException("Cannot process failed settlement");
        }

        if (this.netAmount == null || this.netAmount <= 0) {
            throw new DomainException("Cannot process settlement with zero or negative amount");
        }

        this.status = SettlementStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(String gatewayTransactionId, String gatewayResponse) {
        if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.SETTLED) {
            throw new DomainException("Settlement already completed/settled");
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
            throw new DomainException("Settlement already settled");
        }
        if (this.status != SettlementStatus.COMPLETED) {
            throw new DomainException("Settlement must be completed before settling");
        }

        this.status = SettlementStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.SETTLED) {
            throw new DomainException("Cannot fail completed/settled settlement");
        }

        this.status = SettlementStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == SettlementStatus.COMPLETED || this.status == SettlementStatus.SETTLED) {
            throw new DomainException("Cannot cancel completed/settled settlement");
        }

        this.status = SettlementStatus.CANCELLED;
        this.notes = reason;
        this.updatedAt = LocalDateTime.now();
    }

    private Double calculateNetAmount() {
        return (this.totalAmount != null ? this.totalAmount : 0.0)
                - (this.totalCommission != null ? this.totalCommission : 0.0)
                - (this.totalTax != null ? this.totalTax : 0.0)
                - (this.totalShipping != null ? this.totalShipping : 0.0)
                - (this.totalDiscount != null ? this.totalDiscount : 0.0)
                - (this.totalRefunds != null ? this.totalRefunds : 0.0)
                + (this.adjustmentAmount != null ? this.adjustmentAmount : 0.0);
    }

    public void recalculate() {
        this.netAmount = calculateNetAmount();
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePaymentDetails(
            String paymentMethod,
            String bankName,
            String accountNumber,
            String accountHolderName,
            String ifscCode
    ) {
        if (this.paymentMethod != null) this.paymentMethod = paymentMethod;
        if (this.bankName != null) this.bankName = bankName;
        if (this.accountNumber != null) this.accountNumber = accountNumber;
        if (this.accountHolderName != null) this.accountHolderName = accountHolderName;
        if (this.ifscCode != null) this.ifscCode = ifscCode;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateGatewayDetails(String gatewayReference, String gatewayTransactionId) {
        if (gatewayReference != null) this.gatewayReference = gatewayReference;
        if (gatewayTransactionId != null) this.gatewayTransactionId = gatewayTransactionId;
        this.updatedAt = LocalDateTime.now();
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
        return this.status == SettlementStatus.PENDING || this.status == SettlementStatus.PROCESSED;
    }

    public boolean isTerminal() {
        return this.status == SettlementStatus.COMPLETED ||
                this.status == SettlementStatus.SETTLED ||
                this.status == SettlementStatus.FAILED ||
                this.status == SettlementStatus.CANCELLED;
    }

    public boolean canSettle() {
        return this.status == SettlementStatus.COMPLETED && this.settledAt == null;
    }

    public boolean isBalanced() {
        return this.netAmount != null && this.netAmount == 0.0;
    }

    public Double getNetAmount() {
        return this.netAmount != null ? this.netAmount : 0.0;
    }

    public Integer getTransactionCount() {
        return this.transactionIds != null ? this.transactionIds.size() : 0;
    }

    public Integer getPayoutCount() {
        return this.payoutIds != null ? this.payoutIds.size() : 0;
    }

    public Integer getRefundCount() {
        return this.refundIds != null ? this.refundIds.size() : 0;
    }

    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    public void addNote(String note) {
        this.notes = (this.notes != null ? this.notes + " | " : "") + note;
        this.updatedAt = LocalDateTime.now();
    }

    private static String generateReferenceNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "SET-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }

    @Override
    public String toString() {
        return String.format("Settlement{id=%s, sellerId=%s, status=%s, netAmount=%s, transactions=%d}",
                settlementId, sellerId, status, netAmount, getTransactionCount());
    }
}
