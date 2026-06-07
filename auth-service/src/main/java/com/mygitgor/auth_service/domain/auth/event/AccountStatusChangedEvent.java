package com.mygitgor.auth_service.domain.auth.event;

import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusChangedEvent {
    private String email;
    private String userId;
    private AccountStatus oldStatus;
    private AccountStatus newStatus;
    private String reason;
    private LocalDateTime occurredAt;
}
