package com.mygitgor.seller_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Flat API response containing user identity, assigned role, and account status for authentication clients")
public record UserAuthInfoResponse(

        @Schema(description = "Unique identifier of the authenticated user", example = "usr-4412-xyz")
        @NotBlank(message = "ID cannot be blank")
        String id,

        @Schema(description = "Primary email address of the user", example = "alex.patterson@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Display full name of the user", example = "Alex Patterson")
        @NotBlank(message = "Full name cannot be blank")
        String fullName,

        @Schema(description = "Security role name string", example = "ROLE_CUSTOMER")
        @NotBlank(message = "Role cannot be blank")
        String role,

        @Schema(description = "Flag indicating if the account email has been verified", example = "true")
        boolean emailVerified,

        @Schema(description = "Current lifecycle status of the account", example = "ACTIVE")
        @NotBlank(message = "Account status cannot be blank")
        String accountStatus
) {}
