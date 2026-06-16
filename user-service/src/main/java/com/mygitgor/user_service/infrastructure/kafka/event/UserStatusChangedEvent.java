package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Security and lifecycle domain event emitted immediately after a user's account status changes")
public record UserStatusChangedEvent(

        @Schema(description = "Unique identifier of the user whose status was modified", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "Electronic mail address of the affected user", example = "alex.patterson@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "The previous account status before this modification", example = "PENDING")
        @NotBlank(message = "Old status cannot be blank")
        String oldStatus,

        @Schema(description = "The newly assigned active account status", example = "ACTIVE")
        @NotBlank(message = "New status cannot be blank")
        String newStatus,

        @Schema(description = "The explicit business or security reason behind this status transition", example = "Suspicious activity detected / Terms of Service violation")
        @NotBlank(message = "Reason for change cannot be blank")
        String reason,

        @Schema(description = "Identifier of the actor or automated system that triggered the change", example = "admin-9982")
        @NotBlank(message = "Changed by identifier cannot be blank")
        String changedBy,

        @Schema(description = "Metadata timestamp indicating exactly when the status alteration took effect", example = "2026-06-16T18:31:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}
