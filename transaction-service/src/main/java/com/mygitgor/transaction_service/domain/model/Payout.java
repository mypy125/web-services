package com.mygitgor.transaction_service.domain.model;

import com.mygitgor.transaction_service.domain.model.valueobject.PayoutStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.PayoutType;
import com.mygitgor.transaction_service.shared.exception.DomainException;
import com.mygitgor.transaction_service.shared.valueobject.PayoutId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class Payout {
    private final PayoutId payoutId;
    private final SellerId sellerId;

    // TODO: Payment Details
    private PayoutType type;
    private PayoutStatus status;
    private Double amount;
    private Double fee;
    private Double netAmount;
    private String currency;
    private String description;
    private String referenceNumber;

    // TODO: Payment Method
    private String paymentMethod;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private String ifscCode;
    private String upiId;

    // TODO: transaction References
    private String transactionIds;
    private String settlementId;

    // TODO: Bank Payment Gateway
    private String paymentGateway;
    private String gatewayTransactionId;
    private String gatewayResponse;

    // TODO: Timestamp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;

    // TODO: Metadata
    private String notes;
    private String processedBy;
    private String failureReason;
    private Map<String, String> metadata;

    public static Payout create(SellerId sellerId,
                                Double amount,
                                String paymentMethod,
                                String bankName,
                                String accountNumber,
                                String accountHolderName
    ) {
        LocalDateTime now = LocalDateTime.now();
        Double fee = calculateFee(amount);
        Double netAmount = amount - fee;

        return Payout.builder()
                .payoutId(new PayoutId())
                .sellerId(sellerId)
                .type(PayoutType.SELLER_PAYOUT)
                .status(PayoutStatus.PENDING)
                .amount(amount)
                .fee(fee)
                .netAmount(netAmount)
                .currency("USD")
                .paymentMethod(paymentMethod)
                .bankName(bankName)
                .accountNumber(accountNumber)
                .accountHolderName(accountHolderName)
                .description("Payout to seller")
                .createdAt(now)
                .updatedAt(now)
                .metadata(new HashMap<>())
                .build();
    }

    public static Payout createCommissionPayout(SellerId sellerId,
                                                Double amount,
                                                String description
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Payout.builder()
                .payoutId(new PayoutId())
                .sellerId(sellerId)
                .type(PayoutType.COMMISSION_PAYOUT)
                .status(PayoutStatus.PENDING)
                .amount(amount)
                .fee(0.0)
                .netAmount(amount)
                .currency("USD")
                .description(description != null ? description : "Commission payout")
                .createdAt(now)
                .updatedAt(now)
                .metadata(new HashMap<>())
                .build();
    }

    public static Payout createBonusPayout(SellerId sellerId,
                                           Double amount,
                                           String reason
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Payout.builder()
                .payoutId(new PayoutId())
                .sellerId(sellerId)
                .type(PayoutType.BONUS_PAYOUT)
                .status(PayoutStatus.PENDING)
                .amount(amount)
                .fee(0.0)
                .netAmount(amount)
                .currency("USD")
                .description("Bonus payout: " + reason)
                .createdAt(now)
                .updatedAt(now)
                .metadata(new HashMap<>())
                .build();
    }

    public void process(String processedBy) {
        if (this.status == PayoutStatus.PROCESSED) {
            throw new DomainException("Payout already processed");
        }
        if (this.status == PayoutStatus.COMPLETED) {
            throw new DomainException("Cannot process completed payout");
        }
        if (this.status == PayoutStatus.FAILED) {
            throw new DomainException("Cannot process failed payout");
        }

        this.status = PayoutStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(String gatewayTransactionId, String gatewayResponse) {
        if (this.status == PayoutStatus.COMPLETED) {
            throw new DomainException("Payout already completed");
        }
        if (this.status == PayoutStatus.FAILED) {
            throw new DomainException("Cannot complete failed payout");
        }

        this.status = PayoutStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponse = gatewayResponse;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (this.status == PayoutStatus.COMPLETED) {
            throw new DomainException("Cannot fail completed payout");
        }

        this.status = PayoutStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == PayoutStatus.COMPLETED) {
            throw new DomainException("Cannot cancel completed payout");
        }
        if (this.status == PayoutStatus.CANCELLED) {
            throw new DomainException("Payout already cancelled");
        }

        this.status = PayoutStatus.CANCELLED;
        this.notes = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void addTransaction(String transactionId) {
        if (this.transactionIds == null || this.transactionIds.isEmpty()) {
            this.transactionIds = transactionId;
        } else {
            this.transactionIds = this.transactionIds + "," + transactionId;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBankDetails(String bankName, String accountNumber, String accountHolderName, String ifscCode) {
        if (this.status == PayoutStatus.COMPLETED) {
            throw new DomainException("Cannot update bank details for completed payout");
        }
        if (bankName != null) this.bankName = bankName;
        if (accountNumber != null) this.accountNumber = accountNumber;
        if (accountHolderName != null) this.accountHolderName = accountHolderName;
        if (ifscCode != null) this.ifscCode = ifscCode;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == PayoutStatus.PENDING;
    }

    public boolean isProcessed() {
        return this.status == PayoutStatus.PROCESSED;
    }

    public boolean isCompleted() {
        return this.status == PayoutStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == PayoutStatus.FAILED;
    }

    public boolean isCancelled() {
        return this.status == PayoutStatus.CANCELLED;
    }

    public boolean isActive() {
        return this.status == PayoutStatus.PENDING || this.status == PayoutStatus.PROCESSED;
    }

    public boolean isTerminal() {
        return this.status == PayoutStatus.COMPLETED ||
                this.status == PayoutStatus.FAILED ||
                this.status == PayoutStatus.CANCELLED;
    }

    public Double getNetAmount() {
        return this.netAmount != null ? this.netAmount : this.amount;
    }

    private static Double calculateFee(Double amount) {
        if (amount == null || amount <= 0) return 0.0;
        double fee = amount * 0.005;
        return Math.min(fee, 5.0);
    }

    @Override
    public String toString() {
        return String.format("Payout{id=%s, sellerId=%s, status=%s, amount=%s, netAmount=%s}",
                payoutId, sellerId, status, amount, netAmount);
    }
}
