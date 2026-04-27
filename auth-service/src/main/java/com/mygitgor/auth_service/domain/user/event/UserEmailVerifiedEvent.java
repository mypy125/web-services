package com.mygitgor.auth_service.domain.user.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class UserEmailVerifiedEvent extends ApplicationEvent {
    private final String userId;
    private final String email;
    private final LocalDateTime verifiedAt;

    @Builder
    public UserEmailVerifiedEvent(Object source, String userId, String email, LocalDateTime verifiedAt) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.verifiedAt = verifiedAt;
    }
}
