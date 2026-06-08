package com.mygitgor.auth_service.infrastrucrure.kafka.event.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoggedInEvent {
    private String email;
    private String userId;
    private String token;
    private String role;
    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime occurredAt;
}
