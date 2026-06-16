package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Integration event emitted when a user's review metrics and rating statistics change")
public record ReviewStatsUpdatedEvent(

        @Schema(description = "Unique identifier of the user who wrote the reviews", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The updated total count of reviews written by this user lifetime", example = "15")
        @NotNull(message = "Total reviews count cannot be null")
        @PositiveOrZero(message = "Total reviews must be zero or a positive number")
        Integer totalReviews,

        @Schema(description = "The recalculated mathematical mean of all ratings given by this user", example = "4.8")
        @NotNull(message = "Average rating cannot be null")
        @Min(value = 0, message = "Average rating cannot be less than 0")
        @Max(value = 5, message = "Average rating cannot exceed 5")
        Double averageRating,

        @Schema(description = "Metadata timestamp indicating exactly when this snapshot was captured", example = "2026-06-16T18:28:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}