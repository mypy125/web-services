package com.mygitgor.transaction_service.domain.model.valueobject;

import lombok.Getter;

@Getter
public enum SettlementStatus {
    PENDING("Pending", "Awaiting processing"),
    PROCESSED("Processed", "In processing"),
    COMPLETED("Completed", "Completed"),
    SETTLED("Settled", "Calculated"),
    FAILED("Failed", "Error"),
    CANCELLED("Cancelled", "Cancelled");

    private final String displayName;
    private final String description;

    SettlementStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isActive() {
        return this == PENDING || this == PROCESSED;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == SETTLED || this == FAILED || this == CANCELLED;
    }
}
