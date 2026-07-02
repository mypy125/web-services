package com.mygitgor.user_service.application.dto.request;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request package to update user commerce and order statistics")
public record UpdateOrderStatsRequest(

        @Schema(description = "The total monetary amount of the placed order", example = "99.99")
        @NotNull(message = "Order amount cannot be null")
        @Positive(message = "Order amount must be a positive number")
        Double orderAmount,

        @Schema(description = "Unique identifier of the completed order", example = "ORD-2026-88391")
        @NotBlank(message = "Order ID cannot be blank")
        String orderId,

        @Schema(description = "The exact timestamp when the order was successfully placed", example = "2026-06-15T17:45:30")
        @NotNull(message = "Order date cannot be null")
        LocalDateTime orderDate
) {}
