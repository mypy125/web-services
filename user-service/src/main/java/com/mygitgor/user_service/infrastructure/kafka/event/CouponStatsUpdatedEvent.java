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
public class CouponStatsUpdatedEvent {
    private String userId;
    private Integer couponsUsed;
    private Double totalDiscountReceived;
    private String lastUsedCouponCode;
    private Double lastUsedDiscountAmount;
    private LocalDateTime occurredAt;
}
