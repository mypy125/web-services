package com.mygitgor.auth_service.application.dto.response;

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
@Schema(description = "Token validation response")
public class TokenValidationResponseDto {

    @Schema(description = "Is token valid", example = "true")
    private boolean valid;

    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @Schema(description = "User role", example = "ROLE_CUSTOMER")
    private String role;

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @Schema(description = "Token expiration time")
    private LocalDateTime expiresAt;

    @Schema(description = "Validation message", example = "Token is valid")
    private String message;
}
