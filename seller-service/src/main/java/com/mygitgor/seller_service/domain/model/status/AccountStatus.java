package com.mygitgor.seller_service.domain.model.status;

public enum AccountStatus {
    ACTIVE(true, false),
    PENDING_VERIFICATION(false, true),
    SUSPENDED(false, true),
    BANNED(false, true),
    INACTIVE(false, true);

    private final boolean active;
    private final boolean blocked;

    AccountStatus(boolean active, boolean blocked) {
        this.active = active;
        this.blocked = blocked;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isBlocked() {
        return this == SUSPENDED || this == BANNED;
    }

    public boolean isPending() {
        return this == PENDING_VERIFICATION;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }

    public boolean canSell() {
        return this == ACTIVE;
    }

    public boolean canLogin() {
        return this != BANNED && this != SUSPENDED && this != INACTIVE;
    }

    public String getDisplayName() {
        return switch (this) {
            case ACTIVE -> "Active";
            case PENDING_VERIFICATION -> "Pending Verification";
            case SUSPENDED -> "Suspended";
            case BANNED -> "Banned";
            case INACTIVE -> "Inactive";
        };
    }
}
