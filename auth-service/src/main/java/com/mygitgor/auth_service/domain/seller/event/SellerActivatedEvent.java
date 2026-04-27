package com.mygitgor.auth_service.domain.seller.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class SellerActivatedEvent extends ApplicationEvent {
    private final String sellerId;
    private final String email;
    private final LocalDateTime activatedAt;

    @Builder
    public SellerActivatedEvent(Object source, String sellerId, String email, LocalDateTime activatedAt) {
        super(source);
        this.sellerId = sellerId;
        this.email = email;
        this.activatedAt = activatedAt;
    }
}
