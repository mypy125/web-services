package com.mygitgor.auth_service.domain.user.event;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final String email;
    private final String userId;
    private final UserRole role;
    private final String deviceId;
    private final String ipAddress;
    private final LocalDateTime occurredAt;

    @Builder
    public UserRegisteredEvent(Object source, String email, String userId, UserRole role,
                               String deviceId, String ipAddress, LocalDateTime occurredAt) {
        super(source);
        this.email = email;
        this.userId = userId;
        this.role = role;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.occurredAt = occurredAt;
    }
}