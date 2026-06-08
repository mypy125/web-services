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
public class TokenRefreshedEvent {
    private String email;
    private String userId;
    private String oldToken;
    private String newToken;
    private LocalDateTime occurredAt;
}
