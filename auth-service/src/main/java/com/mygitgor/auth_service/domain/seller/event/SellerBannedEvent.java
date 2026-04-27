package com.mygitgor.auth_service.domain.seller.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class SellerBannedEvent extends ApplicationEvent {
    private final String sellerId;
    private final String reason;
    private final LocalDateTime bannedAt;

    @Builder
    public SellerBannedEvent(Object source, String sellerId, String reason, LocalDateTime bannedAt) {
        super(source);
        this.sellerId = sellerId;
        this.reason = reason;
        this.bannedAt = bannedAt;
    }
}

