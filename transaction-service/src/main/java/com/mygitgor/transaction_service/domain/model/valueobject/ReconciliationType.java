package com.mygitgor.transaction_service.domain.model.valueobject;

import lombok.Getter;

@Getter
public enum ReconciliationType {
    DAILY("Daily", "Daily"),
    WEEKLY("Weekly", "Weekly"),
    MONTHLY("Monthly", "Monthly"),
    QUARTERLY("Quarterly", "Quarterly"),
    YEARLY("Yearly", "Yearly"),
    CUSTOM("Custom", "Custom");

    private final String displayName;
    private final String description;

    ReconciliationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
