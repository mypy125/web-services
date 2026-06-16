package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Integration event emitted when a user's core financial and order lifecycle statistics are updated")
public record OrderStatsUpdatedEvent(

        @Schema(description = "Unique identifier of the user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The updated total number of orders placed by the user lifetime", example = "15")
        @NotNull(message = "Total orders count cannot be null")
        @PositiveOrZero(message = "Total orders must be zero or a positive number")
        Integer totalOrders,

        @Schema(description = "The updated lifetime gross monetary value spent by the user", example = "1450.75")
        @NotNull(message = "Total spent value cannot be null")
        @PositiveOrZero(message = "Total spent must be zero or a positive number")
        Double totalSpent,

        @Schema(description = "The recalculated mathematical mean of the user's order checks", example = "96.71")
        @NotNull(message = "Average order value cannot be null")
        @PositiveOrZero(message = "Average order value must be zero or a positive number")
        Double averageOrderValue,

        @Schema(description = "Timestamp of the latest order that triggered this calculation", example = "2026-06-16T18:24:00")
        @NotNull(message = "Last order date cannot be null")
        LocalDateTime lastOrderDate,

        @Schema(description = "Metadata timestamp indicating exactly when this event fact was captured", example = "2026-06-16T18:24:02")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}