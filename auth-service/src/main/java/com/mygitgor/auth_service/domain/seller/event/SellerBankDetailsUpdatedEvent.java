package com.mygitgor.auth_service.domain.seller.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class SellerBankDetailsUpdatedEvent extends ApplicationEvent {
    private final String sellerId;
    private final LocalDateTime updatedAt;

    @Builder
    public SellerBankDetailsUpdatedEvent(Object source, String sellerId, LocalDateTime updatedAt) {
        super(source);
        this.sellerId = sellerId;
        this.updatedAt = updatedAt;
    }
}
