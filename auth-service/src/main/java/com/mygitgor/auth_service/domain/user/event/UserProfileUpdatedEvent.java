package com.mygitgor.auth_service.domain.user.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class UserProfileUpdatedEvent extends ApplicationEvent {
    private final String userId;
    private final String oldFullName;
    private final String newFullName;
    private final String oldPhoneNumber;
    private final String newPhoneNumber;
    private final LocalDateTime updatedAt;

    @Builder
    public UserProfileUpdatedEvent(Object source, String userId, String oldFullName, String newFullName, String oldPhoneNumber, String newPhoneNumber, LocalDateTime updatedAt) {
        super(source);
        this.userId = userId;
        this.oldFullName = oldFullName;
        this.newFullName = newFullName;
        this.oldPhoneNumber = oldPhoneNumber;
        this.newPhoneNumber = newPhoneNumber;
        this.updatedAt = updatedAt;
    }
}
