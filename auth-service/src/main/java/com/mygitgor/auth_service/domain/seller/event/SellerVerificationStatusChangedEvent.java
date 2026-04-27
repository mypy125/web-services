package com.mygitgor.auth_service.domain.seller.event;

import com.mygitgor.auth_service.domain.seller.model.SellerVerificationStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SellerVerificationStatusChangedEvent extends ApplicationEvent {
    private final String eventId;
    private final String sellerId;
    private final String userId;
    private final String email;
    private final String sellerName;
    private final SellerVerificationStatus oldStatus;
    private final SellerVerificationStatus newStatus;
    private final String reason;
    private final String changedBy;
    private final String changedByRole;
    private final String notes;
    private final LocalDateTime changedAt;

    @Builder
    public SellerVerificationStatusChangedEvent(Object source,
                                                String sellerId,
                                                String userId,
                                                String email,
                                                String sellerName,
                                                SellerVerificationStatus oldStatus,
                                                SellerVerificationStatus newStatus,
                                                String reason,
                                                String changedBy,
                                                String changedByRole,
                                                String notes,
                                                LocalDateTime changedAt) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.sellerId = sellerId;
        this.userId = userId;
        this.email = email;
        this.sellerName = sellerName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
        this.changedByRole = changedByRole;
        this.notes = notes;
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
    }

    public boolean isFullyVerified() {
        return newStatus == SellerVerificationStatus.FULLY_VERIFIED;
    }

    public boolean isRejected() {
        return newStatus == SellerVerificationStatus.REJECTED;
    }

    @Override
    public String toString() {
        return String.format("SellerVerificationStatusChangedEvent{sellerId=%s, oldStatus=%s, newStatus=%s, changedAt=%s}",
                sellerId, oldStatus, newStatus, changedAt);
    }
}
