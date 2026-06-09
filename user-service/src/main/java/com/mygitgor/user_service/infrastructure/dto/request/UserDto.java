package com.mygitgor.user_service.infrastructure.dto.request;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String fullName;
    private UserRole role;
    private boolean emailVerified;
    private String profileImage;
    private String phoneNumber;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime emailVerifiedAt;
}
