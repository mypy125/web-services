package com.mygitgor.auth_service.domain.user.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
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
