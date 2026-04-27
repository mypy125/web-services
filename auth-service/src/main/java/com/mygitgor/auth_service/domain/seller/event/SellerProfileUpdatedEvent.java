package com.mygitgor.auth_service.domain.seller.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SellerProfileUpdatedEvent extends ApplicationEvent {
    private final String eventId;
    private final String sellerId;
    private final String userId;
    private final String email;
    private final String oldSellerName;
    private final String newSellerName;
    private final String oldMobile;
    private final String newMobile;
    private final String oldStoreDescription;
    private final String newStoreDescription;
    private final LocalDateTime updatedAt;

    @Builder
    public SellerProfileUpdatedEvent(Object source,
                                     String sellerId,
                                     String userId,
                                     String email,
                                     String oldSellerName,
                                     String newSellerName,
                                     String oldMobile,
                                     String newMobile,
                                     String oldStoreDescription,
                                     String newStoreDescription,
                                     LocalDateTime updatedAt) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.sellerId = sellerId;
        this.userId = userId;
        this.email = email;
        this.oldSellerName = oldSellerName;
        this.newSellerName = newSellerName;
        this.oldMobile = oldMobile;
        this.newMobile = newMobile;
        this.oldStoreDescription = oldStoreDescription;
        this.newStoreDescription = newStoreDescription;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public boolean hasNameChanged() {
        return oldSellerName != null && !oldSellerName.equals(newSellerName);
    }

    public boolean hasMobileChanged() {
        return oldMobile != null && !oldMobile.equals(newMobile);
    }

    @Override
    public String toString() {
        return String.format("SellerProfileUpdatedEvent{sellerId=%s, updatedAt=%s}", sellerId, updatedAt);
    }
}
