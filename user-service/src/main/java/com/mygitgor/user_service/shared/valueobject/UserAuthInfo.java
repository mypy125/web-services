package com.mygitgor.user_service.shared.valueobject;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAuthInfo {
    private final UserId userId;
    private final Email email;
    private final String fullName;
    private final UserRole role;
    private final boolean emailVerified;
    private final AccountStatus accountStatus;
}
