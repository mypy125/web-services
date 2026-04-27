package com.mygitgor.auth_service.domain.auth.model.event;

import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class AccountStatusChangedEvent extends ApplicationEvent {
    private final String email;
    private final String userId;
    private final AccountStatus oldStatus;
    private final AccountStatus newStatus;
    private final String reason;
    private final LocalDateTime occurredAt;

    @Builder
    public AccountStatusChangedEvent(Object source, String email, String userId,
                                     AccountStatus oldStatus, AccountStatus newStatus,
                                     String reason, LocalDateTime occurredAt) {
        super(source);
        this.email = email;
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }
}
