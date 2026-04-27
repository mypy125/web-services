package com.mygitgor.auth_service.domain.auth.model.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class TokenBlacklistedEvent extends ApplicationEvent {
    private final String token;
    private final String email;
    private final String reason;
    private final LocalDateTime occurredAt;

    @Builder
    public TokenBlacklistedEvent(Object source, String token, String email, String reason, LocalDateTime occurredAt) {
        super(source);
        this.token = token;
        this.email = email;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }
}
