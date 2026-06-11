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
public class ActivityUpdatedEvent {
    private String userId;
    private LocalDateTime lastActiveAt;
    private Integer daysActive;
    private Integer consecutiveLoginDays;
    private LocalDateTime occurredAt;
}
