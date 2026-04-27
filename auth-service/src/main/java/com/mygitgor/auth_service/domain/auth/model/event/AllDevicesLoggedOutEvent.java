package com.mygitgor.auth_service.domain.auth.model.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class AllDevicesLoggedOutEvent extends ApplicationEvent {
    private final String email;
    private final String userId;
    private final LocalDateTime occurredAt;

    @Builder
    public AllDevicesLoggedOutEvent(Object source, String email, String userId, LocalDateTime occurredAt) {
        super(source);
        this.email = email;
        this.userId = userId;
        this.occurredAt = occurredAt;
    }
}
