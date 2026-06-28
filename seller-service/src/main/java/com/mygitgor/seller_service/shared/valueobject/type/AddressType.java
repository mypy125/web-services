package com.mygitgor.seller_service.shared.valueobject.type;

import lombok.Getter;

@Getter
public enum AddressType {
    PICKUP("Pickup", "Pickup address"),
    RETURN("Return", "Return address"),
    WAREHOUSE("Warehouse", "Warehouse address"),
    OFFICE("Office", "Office address"),
    SHIPPING("Shipping", "Shipping address"),
    BILLING("Billing", "Billing address"),
    REGISTERED_OFFICE("Registered Office", "Registered office address"),
    BRANCH_OFFICE("Branch Office", "Branch office address"),
    STORE("Store", "Store location"),
    SHOWROOM("Showroom", "Showroom location"),
    OTHER("Other", "Other address type");

    private final String displayName;
    private final String description;

    AddressType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isPickup() {
        return this == PICKUP;
    }

    public boolean isReturn() {
        return this == RETURN;
    }

    public boolean isWarehouse() {
        return this == WAREHOUSE;
    }

    public boolean isOffice() {
        return this == OFFICE || this == REGISTERED_OFFICE || this == BRANCH_OFFICE;
    }

    public boolean isBusiness() {
        return this == REGISTERED_OFFICE || this == BRANCH_OFFICE || this == STORE || this == SHOWROOM;
    }

    public boolean isDelivery() {
        return this == SHIPPING || this == BILLING;
    }

    public static AddressType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return OTHER;
        }
        for (AddressType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName.trim())) {
                return type;
            }
        }
        return OTHER;
    }
}