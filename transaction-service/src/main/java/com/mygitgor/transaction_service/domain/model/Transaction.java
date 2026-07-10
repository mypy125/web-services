package com.mygitgor.transaction_service.domain.model;

import com.mygitgor.transaction_service.domain.model.valueobject.TransactionStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.TransactionType;
import com.mygitgor.transaction_service.shared.exception.DomainException;
import com.mygitgor.transaction_service.shared.valueobject.OrderId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.TransactionId;
import com.mygitgor.transaction_service.shared.valueobject.UserId;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Builder
public class Transaction {
    // TODO: Identification
    private final TransactionId transactionId;
    private final SellerId sellerId;
    private final UserId customerId;
    private final OrderId orderId;

    // TODO: Transaction Details
    private final TransactionType type;
    private TransactionStatus status;
    private final BigDecimal amount;
    private final BigDecimal tax;
    private final BigDecimal commission;
    private final BigDecimal shippingCost;
    private final BigDecimal discount;
    private BigDecimal netAmount;
    private final String currency;
    private String description;
    private String referenceNumber;

    // TODO: Payment Details
    private String paymentMethod;
    private String paymentGateway;
    private String bankReference;

    // TODO: Timestamp
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime refundedAt;

    // TODO: Metadata
    private String notes;
    private String processedBy;
    private String ipAddress;
    private String userAgent;
    private Map<String, String> metadata;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private Transaction(TransactionId transactionId, SellerId sellerId, UserId customerId, OrderId orderId,
                        TransactionType type, TransactionStatus status, BigDecimal amount,
                        BigDecimal tax, BigDecimal commission, BigDecimal shippingCost,
                        BigDecimal discount, BigDecimal netAmount, String currency, String description,
                        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime completedAt
    ) {
        this.transactionId = Objects.requireNonNull(transactionId, "TransactionId cannot be null");
        this.sellerId = Objects.requireNonNull(sellerId, "SellerId cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "OrderId cannot be null");
        this.type = Objects.requireNonNull(type, "TransactionType cannot be null");
        this.status = Objects.requireNonNull(status, "TransactionStatus cannot be null");

        this.customerId = customerId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.tax = tax != null ? tax : BigDecimal.ZERO;
        this.commission = commission != null ? commission : BigDecimal.ZERO;
        this.shippingCost = shippingCost != null ? shippingCost : BigDecimal.ZERO;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        this.netAmount = netAmount != null ? netAmount : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "USD";
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.metadata = new HashMap<>();
    }

    public static Transaction createSale(SellerId sellerId, UserId customerId, OrderId orderId,
                                         BigDecimal amount, BigDecimal tax, BigDecimal commission,
                                         BigDecimal shippingCost, BigDecimal discount
    ) {
        LocalDateTime now = LocalDateTime.now();

        BigDecimal netAmount = amount
                .subtract(tax != null ? tax : BigDecimal.ZERO)
                .subtract(commission != null ? commission : BigDecimal.ZERO)
                .subtract(shippingCost != null ? shippingCost : BigDecimal.ZERO);

        return Transaction.builder()
                .transactionId(new TransactionId())
                .sellerId(sellerId)
                .customerId(customerId)
                .orderId(orderId)
                .type(TransactionType.SALE)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .tax(tax)
                .commission(commission)
                .shippingCost(shippingCost)
                .discount(discount)
                .netAmount(netAmount)
                .currency("USD")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Transaction createRefund(SellerId sellerId,
                                           UserId customerId,
                                           OrderId orderId,
                                           BigDecimal amount,
                                           String reason
    ) {
        LocalDateTime now = LocalDateTime.now();
        return Transaction.builder()
                .transactionId(new TransactionId())
                .sellerId(sellerId)
                .customerId(customerId)
                .orderId(orderId)
                .type(TransactionType.REFUND)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .netAmount(amount)
                .currency("USD")
                .description("Refund for order: " + reason)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Transaction createCommission(SellerId sellerId,
                                               OrderId orderId,
                                               BigDecimal amount,
                                               String description
    ) {
        LocalDateTime now = LocalDateTime.now();
        return Transaction.builder()
                .transactionId(new TransactionId())
                .sellerId(sellerId)
                .orderId(orderId)
                .type(TransactionType.COMMISSION)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .netAmount(amount)
                .currency("USD")
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .completedAt(now)
                .build();
    }

    public void complete() {
        if (this.status == TransactionStatus.COMPLETED) {
            throw new DomainException("Transaction is already in COMPLETED state");
        }
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot complete transaction from terminal state: " + this.status);
        }
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot mark transaction as FAILED from terminal state: " + this.status);
        }
        this.status = TransactionStatus.FAILED;
        this.notes = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsRefunded() {
        if (this.type != TransactionType.SALE) {
            throw new DomainException("Aborted: Only SALE transactions can be transitioned to REFUNDED state");
        }
        if (this.status != TransactionStatus.COMPLETED) {
            throw new DomainException("Aborted: Only COMPLETED transactions can be marked as refunded");
        }
        this.status = TransactionStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Transaction emitRefundTransaction(String reason) {
        if (!isRefundable()) {
            throw new DomainException("This transaction context does not allow emitting a refund payload");
        }
        return createRefund(this.sellerId, this.customerId, this.orderId, this.amount, reason);
    }

    public void updatePaymentDetails(String paymentMethod,
                                     String paymentGateway,
                                     String bankReference
    ) {
        if (this.status.isTerminal()) {
            throw new DomainException("Cannot alter payment attributes of a terminal transaction entity");
        }
        if (paymentMethod != null) this.paymentMethod = paymentMethod;
        if (paymentGateway != null) this.paymentGateway = paymentGateway;
        if (bankReference != null) this.bankReference = bankReference;
        this.updatedAt = LocalDateTime.now();
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public boolean isCompleted() {
        return this.status.isCompleted();
    }

    public boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }

    public boolean isRefundable() {
        return this.type == TransactionType.SALE &&
                this.status == TransactionStatus.COMPLETED &&
                this.refundedAt == null;
    }
}
