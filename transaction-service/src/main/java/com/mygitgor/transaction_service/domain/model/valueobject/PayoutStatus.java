package com.mygitgor.transaction_service.domain.model.valueobject;

import lombok.Getter;

@Getter
public enum PayoutStatus {
    PENDING("Pending", "Awaiting processing"),
    PROCESSED("Processed", "In processing"),
    COMPLETED("Completed", "Completed"),
    FAILED("Failed", "Error"),
    CANCELLED("Cancelled", "Cancelled");

    private final String displayName;
    private final String description;

    PayoutStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isActive() {
        return this == PENDING || this == PROCESSED;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
