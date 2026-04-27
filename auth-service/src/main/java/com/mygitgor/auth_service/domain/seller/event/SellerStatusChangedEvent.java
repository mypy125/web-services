package com.mygitgor.auth_service.domain.seller.event;


import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SellerStatusChangedEvent extends ApplicationEvent {
    private final String eventId;
    private final String sellerId;
    private final String userId;
    private final String email;
    private final String sellerName;
    private final UserRole role;
    private final AccountStatus oldStatus;
    private final AccountStatus newStatus;
    private final String reason;
    private final String changedBy;
    private final String changedByRole;
    private final String ipAddress;
    private final String userAgent;
    private final String notes;
    private final boolean requiresNotification;
    private final LocalDateTime changedAt;

    @Builder
    public SellerStatusChangedEvent(Object source,
                                    String sellerId,
                                    String userId,
                                    String email,
                                    String sellerName,
                                    UserRole role,
                                    AccountStatus oldStatus,
                                    AccountStatus newStatus,
                                    String reason,
                                    String changedBy,
                                    String changedByRole,
                                    String ipAddress,
                                    String userAgent,
                                    String notes,
                                    boolean requiresNotification,
                                    LocalDateTime changedAt) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.sellerId = sellerId;
        this.userId = userId;
        this.email = email;
        this.sellerName = sellerName;
        this.role = role;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
        this.changedByRole = changedByRole;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.notes = notes;
        this.requiresNotification = requiresNotification;
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
    }

    public boolean isActivation() {
        return oldStatus == AccountStatus.PENDING_VERIFICATION && newStatus == AccountStatus.ACTIVE;
    }

    public boolean isSuspension() {
        return newStatus == AccountStatus.SUSPENDED;
    }

    public boolean isBan() {
        return newStatus == AccountStatus.BANNED;
    }

    @Override
    public String toString() {
        return String.format("SellerStatusChangedEvent{sellerId=%s, oldStatus=%s, newStatus=%s, changedAt=%s}",
                sellerId, oldStatus, newStatus, changedAt);
    }
}
