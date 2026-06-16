package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Core domain event emitted immediately after a new user account is successfully registered")
public record UserCreatedEvent(

        @Schema(description = "Unique identifier of the newly created user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "Primary electronic mail address", example = "john.doe@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Full name of the registered user", example = "John Doe")
        @NotBlank(message = "Full name cannot be blank")
        String fullName,

        @Schema(description = "Initial security role assigned to the user", example = "ROLE_CUSTOMER")
        @NotBlank(message = "Role cannot be blank")
        String role,

        @Schema(description = "Contact phone number provided during registration", example = "+37499112233")
        String phoneNumber,

        @Schema(description = "Initial profile image url or path", example = "avatars/default.jpg")
        String profileImage,

        @Schema(description = "Metadata timestamp indicating exactly when the user was persisted", example = "2026-06-16T18:29:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}
