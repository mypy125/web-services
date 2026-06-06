package com.mygitgor.auth_service.domain.user.event;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleChangedEvent {
    private String email;
    private String userId;
    private UserRole newRole;
    private String updatedBy;
    private LocalDateTime occurredAt;
}
