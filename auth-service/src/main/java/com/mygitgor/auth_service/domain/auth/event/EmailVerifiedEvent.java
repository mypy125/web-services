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
public class EmailVerifiedEvent {
    private String email;
    private String userId;
    private LocalDateTime verifiedAt;
    private LocalDateTime occurredAt;
}
