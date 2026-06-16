package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Domain event published when a user's platform activity metrics are recalculated")
public record ActivityUpdatedEvent(

        @Schema(description = "Unique identifier of the tracked user", example = "usr-4412-xkg")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The exact timestamp of the user's latest interaction", example = "2026-06-16T15:30:00")
        @NotNull(message = "Last active timestamp cannot be null")
        LocalDateTime lastActiveAt,

        @Schema(description = "Total cumulative days the user has been active on the platform", example = "120")
        @NotNull(message = "Days active count cannot be null")
        @PositiveOrZero(message = "Days active must be zero or a positive number")
        Integer daysActive,

        @Schema(description = "Current running streak of consecutive days logged in", example = "7")
        @NotNull(message = "Consecutive login days cannot be null")
        @PositiveOrZero(message = "Consecutive login days must be zero or a positive number")
        Integer consecutiveLoginDays,

        @Schema(description = "The metadata timestamp indicating when this event was emitted", example = "2026-06-16T15:30:02")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}
