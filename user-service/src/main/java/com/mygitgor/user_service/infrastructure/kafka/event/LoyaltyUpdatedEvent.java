package com.mygitgor.user_service.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyUpdatedEvent {
    private String userId;
    private String oldTier;
    private String newTier;
    private Integer loyaltyPoints;
    private Integer pointsChange;
    private LocalDateTime occurredAt;
}
