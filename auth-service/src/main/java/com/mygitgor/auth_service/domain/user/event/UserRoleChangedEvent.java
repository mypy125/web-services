package com.mygitgor.auth_service.domain.user.event;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class UserRoleChangedEvent extends ApplicationEvent {
    private final String email;
    private final String userId;
    private final UserRole oldRole;
    private final UserRole newRole;
    private final String updatedBy;
    private final LocalDateTime occurredAt;

    @Builder
    public UserRoleChangedEvent(Object source, String email, String userId,
                                UserRole oldRole, UserRole newRole,
                                String updatedBy, LocalDateTime occurredAt) {
        super(source);
        this.email = email;
        this.userId = userId;
        this.oldRole = oldRole;
        this.newRole = newRole;
        this.updatedBy = updatedBy;
        this.occurredAt = occurredAt;
    }
}
