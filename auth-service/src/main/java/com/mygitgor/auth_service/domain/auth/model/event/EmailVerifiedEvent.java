package com.mygitgor.auth_service.domain.auth.model.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class EmailVerifiedEvent extends ApplicationEvent {
    private final String email;
    private final String userId;
    private final LocalDateTime verifiedAt;
    private final LocalDateTime occurredAt;

    @Builder
    public EmailVerifiedEvent(Object source, String email, String userId, LocalDateTime verifiedAt, LocalDateTime occurredAt) {
        super(source);
        this.email = email;
        this.userId = userId;
        this.verifiedAt = verifiedAt;
        this.occurredAt = occurredAt;
    }
}
