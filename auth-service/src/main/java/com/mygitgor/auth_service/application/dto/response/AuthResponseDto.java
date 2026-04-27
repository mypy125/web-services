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
@Schema(description = "Authentication response")
public class AuthResponseDto {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;

    @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String refreshToken;

    @Schema(description = "Response message", example = "Login successful")
    private String message;

    @Schema(description = "User role", example = "ROLE_CUSTOMER")
    private UserRole role;

    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Token expiration time")
    private LocalDateTime expiresAt;

    @Schema(description = "Response timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Requires email verification", example = "false")
    private boolean requiresEmailVerification;

    @Schema(description = "Account status", example = "ACTIVE")
    private String accountStatus;

    public AuthResponseDto(String token, String message, UserRole role, String email, String userId) {
        this.token = token;
        this.message = message;
        this.role = role;
        this.email = email;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }
}
