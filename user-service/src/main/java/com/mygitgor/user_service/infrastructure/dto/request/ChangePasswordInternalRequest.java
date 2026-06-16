package com.mygitgor.user_service.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to change user password internally")
public record ChangePasswordInternalRequest(

        @Schema(description = "New secure password for the user", example = "dB9!kLp4mQ")
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String newPassword
) {}
