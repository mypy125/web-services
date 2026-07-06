package com.mygitgor.transaction_service.domain.model.valueobject;

import lombok.Getter;

@Getter
public enum TransactionType {
    SALE("Sale", "Sale", "+"),
    REFUND("Refund", "Return", "-"),
    COMMISSION("Commission", "Commission", "-"),
    PAYOUT("Payout", "Pay", "-"),
    WITHDRAWAL("Withdrawal", "Withdrawal of funds", "-"),
    ADJUSTMENT("Adjustment", "Correction", "+/-"),
    FEE("Fee", "Collection", "-"),
    BONUS("Bonus", "Bonus", "+"),
    REVENUE("Revenue", "Income", "+");

    private final String displayName;
    private final String description;
    private final String sign;

    TransactionType(String displayName, String description, String sign) {
        this.displayName = displayName;
        this.description = description;
        this.sign = sign;
    }

    public boolean isIncome() {
        return this == SALE || this == BONUS || this == REVENUE;
    }

    public boolean isExpense() {
        return this == REFUND || this == COMMISSION || this == PAYOUT ||
                this == WITHDRAWAL || this == FEE;
    }
}