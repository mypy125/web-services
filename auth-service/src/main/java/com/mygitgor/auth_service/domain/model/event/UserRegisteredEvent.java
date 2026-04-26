package com.mygitgor.auth_service.domain.model.event;

import com.mygitgor.auth_service.domain.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final String email;
    private final String userId;
    private final UserRole role;
    private final LocalDateTime occurredAt;

    @Builder
    public UserRegisteredEvent(Object source, String email, String userId, UserRole role, LocalDateTime occurredAt) {
        super(source);
        this.email = email;
        this.userId = userId;
        this.role = role;
        this.occurredAt = occurredAt;
    }
}
