package com.mygitgor.user_service.infrastructure.dto.response;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthInfo {
    private UserId userId;
    private Email email;
    private String fullName;
    private UserRole role;
    private boolean emailVerified;
    private AccountStatus accountStatus;
}

