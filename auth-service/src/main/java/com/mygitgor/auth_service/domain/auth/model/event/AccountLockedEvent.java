package com.mygitgor.auth_service.domain.auth.model.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class AccountLockedEvent extends ApplicationEvent {
    private final String email;
    private final String reason;
    private final LocalDateTime lockedUntil;
    private final LocalDateTime occurredAt;

    @Builder
    public AccountLockedEvent(Object source, String email, String reason, LocalDateTime lockedUntil, LocalDateTime occurredAt) {
        super(source);
        this.email = email;
        this.reason = reason;
        this.lockedUntil = lockedUntil;
        this.occurredAt = occurredAt;
    }
}

