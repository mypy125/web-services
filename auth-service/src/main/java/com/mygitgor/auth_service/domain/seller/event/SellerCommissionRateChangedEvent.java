package com.mygitgor.auth_service.domain.seller.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SellerCommissionRateChangedEvent extends ApplicationEvent {
    private final String eventId;
    private final String sellerId;
    private final double oldRate;
    private final double newRate;
    private final String updatedBy;
    private final LocalDateTime updatedAt;

    @Builder
    public SellerCommissionRateChangedEvent(Object source,
                                            String sellerId,
                                            double oldRate,
                                            double newRate,
                                            String updatedBy,
                                            LocalDateTime updatedAt) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.sellerId = sellerId;
        this.oldRate = oldRate;
        this.newRate = newRate;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public boolean isRateIncreased() {
        return newRate > oldRate;
    }

    public boolean isRateDecreased() {
        return newRate < oldRate;
    }

    public double getRateDifference() {
        return newRate - oldRate;
    }
}
