package com.mygitgor.auth_service.infrastrucrure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private String email;
    private String userId;
    private String name;
    private String role;
    private String deviceId;
    private String ipAddress;
    private LocalDateTime occurredAt;
}
