package com.mygitgor.auth_service.domain.seller.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class SellerBusinessUpdatedEvent extends ApplicationEvent {
    private final String sellerId;
    private final String oldBusinessName;
    private final String newBusinessName;
    private final LocalDateTime updatedAt;

    @Builder
    public SellerBusinessUpdatedEvent(Object source, String sellerId, String oldBusinessName,
                                      String newBusinessName, LocalDateTime updatedAt) {
        super(source);
        this.sellerId = sellerId;
        this.oldBusinessName = oldBusinessName;
        this.newBusinessName = newBusinessName;
        this.updatedAt = updatedAt;
    }
}
