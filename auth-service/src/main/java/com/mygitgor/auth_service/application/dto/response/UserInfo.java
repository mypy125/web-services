package com.mygitgor.auth_service.application.dto.response;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private String email;
    private String userId;
    private UserRole role;
    private String fullName;
    private boolean emailVerified;
}
