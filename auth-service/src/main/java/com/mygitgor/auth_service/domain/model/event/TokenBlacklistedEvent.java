package com.mygitgor.auth_service.domain.model.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class TokenBlacklistedEvent extends ApplicationEvent {
    private final String token;
    private final String email;
    private final LocalDateTime occurredAt;

    @Builder
    public TokenBlacklistedEvent(Object source, String token, String email, LocalDateTime occurredAt) {
        super(source);
        this.token = token;
        this.email = email;
        this.occurredAt = occurredAt;
    }
}
