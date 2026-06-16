package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Integration event emitted when a user's shopping cart contents change")
public record CartStatsUpdatedEvent(

        @Schema(description = "Unique identifier of the user who owns the cart", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The current total number of items left in the user's cart", example = "3")
        @NotNull(message = "Cart items count cannot be null")
        @PositiveOrZero(message = "Cart items count must be zero or a positive number")
        Integer cartItemsCount,

        @Schema(description = "The exact timestamp when the cart modification occurred", example = "2026-06-16T18:14:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}
