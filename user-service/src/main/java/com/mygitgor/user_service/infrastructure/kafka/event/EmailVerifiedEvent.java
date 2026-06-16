package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Security domain event emitted immediately after a user successfully verifies their email address")
public record EmailVerifiedEvent(

        @Schema(description = "Unique identifier of the verified user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The electronic mail address that was verified", example = "john.doe@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "The exact timestamp when the verification token was validated", example = "2026-06-16T18:15:00")
        @NotNull(message = "Verification timestamp cannot be null")
        LocalDateTime verifiedAt,

        @Schema(description = "Metadata timestamp indicating when this event instance was created", example = "2026-06-16T18:15:01")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}
