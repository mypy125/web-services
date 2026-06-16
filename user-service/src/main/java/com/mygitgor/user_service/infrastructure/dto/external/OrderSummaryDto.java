package com.mygitgor.user_service.infrastructure.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Lightweight data transfer object representing an overview of a user order")
public record OrderSummaryDto(

        @Schema(description = "Unique identifier of the order record", example = "ord-5512-abc")
        @NotBlank(message = "Order ID cannot be blank")
        String id,

        @Schema(description = "Human-readable unique business order number", example = "ORD-2026-00482")
        @NotBlank(message = "Order number cannot be blank")
        String orderNumber,

        @Schema(description = "Total monetary amount billed for the order", example = "149.50")
        @NotNull(message = "Total amount cannot be null")
        @PositiveOrZero(message = "Total amount must be zero or a positive number")
        Double totalAmount,

        @Schema(description = "Current lifecycle status of the order", example = "DELIVERED")
        @NotBlank(message = "Status cannot be blank")
        String status,

        @Schema(description = "Total number of physical items or products within the order", example = "3")
        @NotNull(message = "Items count cannot be null")
        @Min(value = 1, message = "Items count must be at least 1")
        Integer itemsCount,

        @Schema(description = "Timestamp when the order was originally placed", example = "2026-06-15T18:00:00")
        @NotNull(message = "Creation timestamp cannot be null")
        LocalDateTime createdAt
) {}
