package com.mygitgor.auth_service.domain.seller.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class SellerEmailVerifiedEvent extends ApplicationEvent {
    private final String sellerId;
    private final String email;
    private final LocalDateTime verifiedAt;

    @Builder
    public SellerEmailVerifiedEvent(Object source, String sellerId, String email, LocalDateTime verifiedAt) {
        super(source);
        this.sellerId = sellerId;
        this.email = email;
        this.verifiedAt = verifiedAt;
    }
}