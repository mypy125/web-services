package com.mygitgor.user_service.infrastructure.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Lightweight data transfer object representing an order summary snapshot")
public record OrderSummaryDto(

        @Schema(description = "Unique identifier of the order", example = "ord-9982-abc")
        @NotBlank(message = "Order ID cannot be blank")
        String id,

        @Schema(description = "Human-readable unique business order number", example = "ORD-2026-00142")
        @NotBlank(message = "Order number cannot be blank")
        String orderNumber,

        @Schema(description = "Total gross monetary value of the order", example = "149.99")
        @NotNull @PositiveOrZero(message = "Total amount must be positive or zero")
        Double totalAmount,

        @Schema(description = "Current lifecycle state of the order", example = "PROCESSING")
        @NotBlank(message = "Status cannot be blank")
        String status,

        @Schema(description = "Total count of unique physical line items in the order", example = "3")
        @NotNull @PositiveOrZero(message = "Items count must be positive or zero")
        Integer itemsCount,

        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime updatedAt,

        @Schema(description = "Three-letter ISO currency code", example = "USD")
        @NotBlank(message = "Currency cannot be blank")
        String currency,

        @Schema(description = "Current payment state", example = "PAID")
        @NotBlank(message = "Payment status cannot be blank")
        String paymentStatus,

        @Schema(description = "Current logistics delivery state", example = "SHIPPED")
        @NotBlank(message = "Delivery status cannot be blank")
        String deliveryStatus
) {}
