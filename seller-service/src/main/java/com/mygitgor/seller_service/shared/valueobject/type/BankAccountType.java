package com.mygitgor.seller_service.shared.valueobject.type;

import lombok.Getter;

@Getter
public enum BankAccountType {
    SAVINGS("Savings", "Personal savings account"),
    CHECKING("Checking", "Personal checking account"),
    CURRENT("Current", "Personal current account"),

    BUSINESS_SAVINGS("Business Savings", "Business savings account"),
    BUSINESS_CHECKING("Business Checking", "Business checking account"),
    BUSINESS_CURRENT("Business Current", "Business current account"),

    MONEY_MARKET("Money Market", "Money market account"),
    CERTIFICATE_OF_DEPOSIT("Certificate of Deposit", "Certificate of deposit account"),

    PAYPAL("PayPal", "PayPal merchant account"),
    STRIPE("Stripe", "Stripe merchant account"),
    UPI("UPI", "Unified Payments Interface account"),

    OTHER("Other", "Other account type");

    private final String displayName;
    private final String description;

    BankAccountType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isSavings() {
        return this == SAVINGS || this == BUSINESS_SAVINGS;
    }

    public boolean isChecking() {
        return this == CHECKING || this == BUSINESS_CHECKING;
    }

    public boolean isCurrent() {
        return this == CURRENT || this == BUSINESS_CURRENT;
    }

    public boolean isBusiness() {
        return this == BUSINESS_SAVINGS || this == BUSINESS_CHECKING || this == BUSINESS_CURRENT;
    }

    public boolean isPersonal() {
        return this == SAVINGS || this == CHECKING || this == CURRENT;
    }

    public boolean isDigital() {
        return this == PAYPAL || this == STRIPE || this == UPI;
    }

    public static BankAccountType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return OTHER;
        }
        for (BankAccountType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName.trim())) {
                return type;
            }
        }
        return OTHER;
    }
}