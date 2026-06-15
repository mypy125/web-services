package com.mygitgor.user_service.domain.model;

public enum AccountStatus {
    PENDING_VERIFICATION, BANNED, SUSPENDED, ACTIVE;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isBlocked() {
        return this == SUSPENDED || this == BANNED;
    }
}
