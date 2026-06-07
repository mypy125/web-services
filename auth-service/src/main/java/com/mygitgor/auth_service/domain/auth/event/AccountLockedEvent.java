package com.mygitgor.auth_service.domain.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountLockedEvent {
    private String email;
    private String reason;
    private LocalDateTime lockedUntil;
    private LocalDateTime occurredAt;
}

