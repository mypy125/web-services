package com.mygitgor.user_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to change current user password from profile")
public record ChangePasswordRequest(

        @Schema(description = "The user's current password for verification", example = "OldPassword123!")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "New secure password", example = "NewSecurePassword2026!")
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String newPassword,

        @Schema(description = "Confirmation of the new password", example = "NewSecurePassword2026!")
        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {

    public boolean isPasswordConfirmationValid() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
