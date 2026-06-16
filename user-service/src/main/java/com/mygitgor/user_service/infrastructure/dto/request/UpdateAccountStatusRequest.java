package com.mygitgor.user_service.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Management request to update a user's account status")
public record UpdateAccountStatusRequest(

        @Schema(description = "The new status for the account", example = "BANNED")
        @NotBlank(message = "Status is required")
        String status,

        @Schema(description = "The reason for changing the account status", example = "Violation of terms of service (Spam)")
        String reason,

        @Schema(description = "Identifier or username of the operator who initiated the change", example = "admin_moderator_4")
        String changedBy
) {}
