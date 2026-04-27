package com.mygitgor.auth_service.domain.user.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class UserLoggedInEvent extends ApplicationEvent {
    private final String email;
    private final String userId;
    private final String token;
    private final String deviceId;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime occurredAt;

    @Builder
    public UserLoggedInEvent(Object source, String email, String userId, String token,
                             String deviceId, String ipAddress, String userAgent,
                             LocalDateTime occurredAt) {
        super(source);
        this.email = email;
        this.userId = userId;
        this.token = token;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.occurredAt = occurredAt;
    }
}
