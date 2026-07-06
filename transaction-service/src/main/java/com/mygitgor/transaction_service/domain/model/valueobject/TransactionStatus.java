package com.mygitgor.transaction_service.domain.model.valueobject;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    PENDING("Pending", "Awaiting processing"),
    PROCESSING("Processing", "In processing"),
    COMPLETED("Completed", "Completed"),
    FAILED("Failed", "Error"),
    REFUNDED("Refunded", "Returned"),
    CANCELLED("Cancelled", "Cancelled"),
    SETTLED("Settled", "Calculated");

    private final String displayName;
    private final String description;

    TransactionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isCompleted() {
        return this == COMPLETED || this == SETTLED;
    }

    public boolean isActive() {
        return this == PENDING || this == PROCESSING;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == REFUNDED || this == CANCELLED || this == SETTLED;
    }
}
