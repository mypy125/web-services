package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Integration event emitted to synchronize core financial and transactional metrics across microservices")
public record UserOrderStatsUpdatedEvent(

        @Schema(description = "Unique identifier of the user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "Electronic mail address of the account owner", example = "john.doe@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "The absolute lifetime count of non-cancelled orders", example = "12")
        @NotNull(message = "Total orders count cannot be null")
        @PositiveOrZero(message = "Total orders count must be zero or a positive number")
        Integer totalOrdersCount,

        @Schema(description = "The total gross monetary value spent by the user lifetime", example = "850.25")
        @NotNull(message = "Total spent amount cannot be null")
        @PositiveOrZero(message = "Total spent amount must be zero or a positive number")
        Double totalSpentAmount,

        @Schema(description = "Metadata timestamp indicating exactly when this statistics snapshot was frozen", example = "2026-06-16T21:38:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}
