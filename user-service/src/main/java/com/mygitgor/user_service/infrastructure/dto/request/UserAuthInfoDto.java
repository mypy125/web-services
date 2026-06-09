package com.mygitgor.user_service.infrastructure.dto.request;

import com.mygitgor.user_service.domain.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthInfoDto {
    private String id;
    private String email;
    private String fullName;
    private UserRole role;
    private boolean emailVerified;
}
