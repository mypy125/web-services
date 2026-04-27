package com.mygitgor.auth_service.domain.seller.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class SellerSuspendedEvent extends ApplicationEvent {
    private final String sellerId;
    private final String reason;
    private final LocalDateTime suspendedAt;

    @Builder
    public SellerSuspendedEvent(Object source, String sellerId, String reason, LocalDateTime suspendedAt) {
        super(source);
        this.sellerId = sellerId;
        this.reason = reason;
        this.suspendedAt = suspendedAt;
    }
}
