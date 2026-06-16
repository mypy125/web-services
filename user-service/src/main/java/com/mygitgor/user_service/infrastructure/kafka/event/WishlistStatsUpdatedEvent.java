package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Integration event emitted when a user's wishlist contents and counters are updated")
public record WishlistStatsUpdatedEvent(

        @Schema(description = "Unique identifier of the user who owns the wishlist", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The current total number of items saved in the user's wishlist", example = "23")
        @NotNull(message = "Wishlist items count cannot be null")
        @PositiveOrZero(message = "Wishlist items count must be zero or a positive number")
        Integer wishlistItemsCount,

        @Schema(description = "Metadata timestamp indicating exactly when the wishlist alteration occurred", example = "2026-06-16T18:35:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}