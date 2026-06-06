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
public class UserEmailVerifiedEvent {
    private String userId;
    private String email;
    private LocalDateTime verifiedAt;
}
