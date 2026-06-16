package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Domain event published whenever a user's loyalty tier upgrades/downgrades or points balance shifts")
public record LoyaltyUpdatedEvent(

        @Schema(description = "Unique identifier of the user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The previous loyalty tier name before this change", example = "SILVER")
        @NotBlank(message = "Old tier cannot be blank")
        String oldTier,

        @Schema(description = "The newly assigned loyalty tier name", example = "GOLD")
        @NotBlank(message = "New tier cannot be blank")
        String newTier,

        @Schema(description = "The absolute total of loyalty points available AFTER the update", example = "1050")
        @NotNull(message = "Loyalty points total cannot be null")
        Integer loyaltyPoints,

        @Schema(description = "The delta value showing how many points were added or deducted", example = "150")
        @NotNull(message = "Points change delta cannot be null")
        Integer pointsChange,

        @Schema(description = "The exact timestamp when the loyalty recalculation occurred", example = "2026-06-16T18:22:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}
