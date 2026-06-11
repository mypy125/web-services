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
public class UserStatusChangedEvent {
    private String userId;
    private String email;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private String changedBy;
    private LocalDateTime occurredAt;
}
