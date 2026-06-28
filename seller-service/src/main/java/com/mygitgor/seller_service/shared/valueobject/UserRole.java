package com.mygitgor.seller_service.shared.valueobject;

import lombok.Getter;

@Getter
public enum UserRole {
    ROLE_CUSTOMER("CUSTOMER", "Customer"),
    ROLE_SELLER("SELLER", "Seller"),
    ROLE_ADMIN("ADMIN", "Administrator"),
    ROLE_MODERATOR("MODERATOR", "Moderator"),
    ROLE_ANALYST("ANALYST", "Analyst"),
    ROLE_MANAGER("MANAGER", "Manager"),
    ROLE_SUPPORT("SUPPORT", "Support");

    private final String code;
    private final String displayName;

    UserRole(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public boolean isAdmin() {
        return this == ROLE_ADMIN;
    }

    public boolean isSeller() {
        return this == ROLE_SELLER;
    }

    public boolean isCustomer() {
        return this == ROLE_CUSTOMER;
    }

    public boolean isModerator() {
        return this == ROLE_MODERATOR;
    }

    public boolean isAnalyst() {
        return this == ROLE_ANALYST;
    }

    public boolean isManager() {
        return this == ROLE_MANAGER;
    }

    public boolean isSupport() {
        return this == ROLE_SUPPORT;
    }

    public boolean hasElevatedPrivileges() {
        return this == ROLE_ADMIN || this == ROLE_MODERATOR || this == ROLE_MANAGER;
    }

    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equalsIgnoreCase(code)) {
                return role;
            }
        }
        return ROLE_CUSTOMER;
    }
}
