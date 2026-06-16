package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Core domain event emitted immediately after a user's profile details are successfully updated")
public record UserUpdatedEvent(

        @Schema(description = "Unique identifier of the updated user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The updated primary electronic mail address", example = "alex.patterson@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "The updated full display name of the user", example = "Alex Patterson")
        @NotBlank(message = "Full name cannot be blank")
        String fullName,

        @Schema(description = "The current security role assigned to the user", example = "ROLE_CUSTOMER")
        @NotBlank(message = "Role cannot be blank")
        String role,

        @Schema(description = "Metadata timestamp indicating exactly when the profile alteration was persisted", example = "2026-06-16T18:32:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}