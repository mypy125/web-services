package com.mygitgor.transaction_service.domain.model.valueobject;

import lombok.Getter;

@Getter
public enum SettlementType {
    DAILY("Daily", "Daily calculation"),
    WEEKLY("Weekly", "Weekly calculation"),
    BI_WEEKLY("Bi-Weekly", "Two-week calculation"),
    MONTHLY("Monthly", "Monthly calculation"),
    QUARTERLY("Quarterly", "Quarterly calculation"),
    MANUAL("Manual", "Manual calculation");

    private final String displayName;
    private final String description;

    SettlementType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
