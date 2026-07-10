package com.mygitgor.transaction_service.domain.model;

import com.mygitgor.transaction_service.domain.model.valueobject.PayoutStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.PayoutType;
import com.mygitgor.transaction_service.shared.exception.DomainException;
import com.mygitgor.transaction_service.shared.valueobject.PayoutId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Builder
public class Payout {
    private final PayoutId payoutId;
    private final SellerId sellerId;

    // TODO: Payment Details
    private final PayoutType type;
    private PayoutStatus status;
    private final BigDecimal amount;
    private final BigDecimal fee;
    private final BigDecimal netAmount;
    private final String currency;
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
    private Set<String> transactionIds;
    private String settlementId;

    // TODO: Bank Payment Gateway
    private String paymentGateway;
    private String gatewayTransactionId;
    private String gatewayResponse;

    // TODO: Timestamp
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;

    // TODO: Metadata
    private String notes;
    private String processedBy;
    private String failureReason;
    private Map<String, String> metadata;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private Payout(PayoutId payoutId, SellerId sellerId, PayoutType type,
                   PayoutStatus status, BigDecimal amount, BigDecimal fee,
                   BigDecimal netAmount, String currency, String description,
                   String paymentMethod, String bankName, String accountNumber,
                   String accountHolderName, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.payoutId = Objects.requireNonNull(payoutId, "PayoutId cannot be null");
        this.sellerId = Objects.requireNonNull(sellerId, "SellerId cannot be null");
        this.type = Objects.requireNonNull(type, "PayoutType cannot be null");
        this.status = Objects.requireNonNull(status, "PayoutStatus cannot be null");
        this.amount = amount;
        this.fee = fee;
        this.netAmount = netAmount;
        this.currency = currency != null ? currency : "USD";
        this.description = description;
        this.paymentMethod = paymentMethod;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.transactionIds = new LinkedHashSet<>();
        this.metadata = new HashMap<>();
    }

    public static Payout create(SellerId sellerId,
                                BigDecimal amount,
                                String paymentMethod,
                                String bankName,
                                String accountNumber,
                                String accountHolderName
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Payout amount must be positive");
        }
        LocalDateTime now = LocalDateTime.now();
        BigDecimal fee = calculateFee(amount);
        BigDecimal netAmount = amount.subtract(fee);

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
                .build();
    }

    public static Payout createCommissionPayout(SellerId sellerId,
                                                BigDecimal amount,
                                                String description
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Commission amount must be positive");
        }
        LocalDateTime now = LocalDateTime.now();

        return Payout.builder()
                .payoutId(new PayoutId())
                .sellerId(sellerId)
                .type(PayoutType.COMMISSION_PAYOUT)
                .status(PayoutStatus.PENDING)
                .amount(amount)
                .fee(BigDecimal.ZERO)
                .netAmount(amount)
                .currency("USD")
                .description(description != null ? description : "Commission payout")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Payout createBonusPayout(SellerId sellerId,
                                           BigDecimal amount,
                                           String reason
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Bonus amount must be positive");
        }
        LocalDateTime now = LocalDateTime.now();

        return Payout.builder()
                .payoutId(new PayoutId())
                .sellerId(sellerId)
                .type(PayoutType.BONUS_PAYOUT)
                .status(PayoutStatus.PENDING)
                .amount(amount)
                .fee(BigDecimal.ZERO)
                .netAmount(amount)
                .currency("USD")
                .description("Bonus payout: " + reason)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void process(String processedBy) {
        ensureNotTerminalStatus("process");
        if (this.status == PayoutStatus.PROCESSED) {
            throw new DomainException("Payout already processed");
        }

        this.status = PayoutStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(String gatewayTransactionId, String gatewayResponse) {
        ensureNotTerminalStatus("complete");

        this.status = PayoutStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponse = gatewayResponse;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot fail a payout that is already in a terminal state");
        }

        this.status = PayoutStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot cancel a payout that is already in a terminal state");
        }

        this.status = PayoutStatus.CANCELLED;
        this.notes = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void addTransaction(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) return;
        if (this.transactionIds == null) {
            this.transactionIds = new LinkedHashSet<>();
        }
        this.transactionIds.add(transactionId);
        this.updatedAt = LocalDateTime.now();
    }

    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBankDetails(String bankName,
                                  String accountNumber,
                                  String accountHolderName,
                                  String ifscCode
    ) {
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot update bank details for a payout in terminal state");
        }
        if (bankName != null) this.bankName = bankName;
        if (accountNumber != null) this.accountNumber = accountNumber;
        if (accountHolderName != null) this.accountHolderName = accountHolderName;
        if (ifscCode != null) this.ifscCode = ifscCode;
        this.updatedAt = LocalDateTime.now();
    }

    public Map<String, String> getMetadata() {
        return metadata == null ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
    }

    public Set<String> getTransactionIds() {
        return transactionIds == null ? Collections.emptySet() : Collections.unmodifiableSet(transactionIds);
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
        return this.status.isActive();
    }

    public boolean isTerminal() {
        return this.status.isTerminal();
    }


    private void ensureNotTerminalStatus(String action) {
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot perform action '" + action + "' because payout is in terminal state: " + this.status);
        }
    }

    private static BigDecimal calculateFee(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal fee = amount.multiply(BigDecimal.valueOf(0.005));
        BigDecimal maxFee = BigDecimal.valueOf(5.0);

        return fee.compareTo(maxFee) > 0 ? maxFee.setScale(2, RoundingMode.HALF_UP) : fee.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("Payout{id=%s, sellerId=%s, status=%s, amount=%s, netAmount=%s}",
                payoutId, sellerId, status, amount, netAmount);
    }
}
