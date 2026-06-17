package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Security domain event emitted immediately when a user's access role or privileges are modified")
public record UserRoleChangedEvent(

        @Schema(description = "Unique identifier of the affected user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "Electronic mail address of the user", example = "alex.patterson@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "The previous role string before this alteration", example = "ROLE_CUSTOMER")
        @NotBlank(message = "Old role cannot be blank")
        String oldRole,

        @Schema(description = "The newly assigned role string granting updated access levels", example = "ROLE_MANAGER")
        @NotBlank(message = "New role cannot be blank")
        String newRole,

        @Schema(description = "Identifier of the administrator or system process that executed the role shift", example = "admin-7712")
        @NotBlank(message = "Changed by identifier cannot be blank")
        String changedBy,

        @Schema(description = "Metadata timestamp indicating exactly when the privilege transition was finalized", example = "2026-06-16T21:35:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}
