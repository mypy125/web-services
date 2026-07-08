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

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class Transaction {
    // TODO: Identification
    private final TransactionId transactionId;
    private final SellerId sellerId;
    private final UserId customerId;
    private final OrderId orderId;

    // TODO: Transaction Details
    private TransactionType type;
    private TransactionStatus status;
    private Double amount;
    private Double tax;
    private Double commission;
    private Double shippingCost;
    private Double discount;
    private Double netAmount;
    private String currency;
    private String description;
    private String referenceNumber;

    // TODO: Payment Details
    private String paymentMethod;
    private String paymentGateway;
    private String bankReference;

    // TODO: Timestamp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime refundedAt;

    // TODO: Metadata
    private String notes;
    private String processedBy;
    private String ipAddress;
    private String userAgent;
    private Map<String, String> metadata;

    public static Transaction createSale(SellerId sellerId,
                                         UserId customerId,
                                         OrderId orderId,
                                         Double amount,
                                         Double tax,
                                         Double commission,
                                         Double shippingCost,
                                         Double discount
    ) {
        LocalDateTime now = LocalDateTime.now();
        Double netAmount = amount - tax - commission - shippingCost - discount;

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
                                           Double amount,
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
                                               Double amount,
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
            throw new DomainException("Transaction already completed");
        }
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (this.status == TransactionStatus.COMPLETED) {
            throw new DomainException("Cannot fail completed transaction");
        }
        this.status = TransactionStatus.FAILED;
        this.notes = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void refund() {
        if (this.type != TransactionType.SALE) {
            throw new DomainException("Only sale transactions can be refunded");
        }
        if (this.status != TransactionStatus.COMPLETED) {
            throw new DomainException("Only completed transactions can be refunded");
        }

        Transaction refund = createRefund(
                this.sellerId,
                this.customerId,
                this.orderId,
                this.amount,
                "Full refund"
        );
        this.refundedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == TransactionStatus.COMPLETED;
    }

    public boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }

    public boolean isRefundable() {
        return this.type == TransactionType.SALE &&
                this.status == TransactionStatus.COMPLETED &&
                this.refundedAt == null;
    }

    public Double getNetAmount() {
        return this.netAmount != null ? this.netAmount : this.amount;
    }
}
