package com.mygitgor.auth_service.domain.user.event;

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
public class UserProfileUpdatedEvent {
    private String userId;
    private String oldFullName;
    private String newFullName;
    private String oldPhoneNumber;
    private String newPhoneNumber;
    private LocalDateTime updatedAt;
}
