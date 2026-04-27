package com.mygitgor.auth_service.application.dto.response;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information response")
public class UserInfoResponseDto {

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User role", example = "ROLE_CUSTOMER")
    private UserRole role;

    @Schema(description = "Is email verified", example = "true")
    private boolean emailVerified;

    @Schema(description = "Account created at")
    private LocalDateTime createdAt;

    @Schema(description = "Last login at")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Account status", example = "ACTIVE")
    private String accountStatus;

    @Schema(description = "Profile image URL", example = "https://example.com/profile.jpg")
    private String profileImage;
}
