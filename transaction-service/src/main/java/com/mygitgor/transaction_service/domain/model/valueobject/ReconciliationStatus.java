package com.mygitgor.transaction_service.domain.model.valueobject;

import lombok.Getter;

@Getter
public enum ReconciliationStatus {
    PENDING("Pending", "Awaiting verification"),
    IN_PROGRESS("In Progress", "In progress"),
    RECONCILED("Reconciled", "Reconciled"),
    REJECTED("Rejected", "Rejected"),
    CANCELLED("Cancelled", "Cancelled");

    private final String displayName;
    private final String description;

    ReconciliationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS;
    }

    public boolean isTerminal() {
        return this == RECONCILED || this == REJECTED || this == CANCELLED;
    }
}
