package com.mygitgor.user_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request package to update user's purchased product statistics")
public record UpdateProductStatsRequest(

        @Schema(description = "Unique identifier of the product", example = "prod-9921-xyz")
        @NotBlank(message = "Product ID cannot be blank")
        String productId,

        @Schema(description = "Display name of the product", example = "Wireless Mechanical Keyboard")
        @NotBlank(message = "Product name cannot be blank")
        String productName,

        @Schema(description = "Commercial category of the product", example = "Electronics")
        @NotBlank(message = "Category cannot be blank")
        String category,

        @Schema(description = "Quantity of the product purchased in this specific event", example = "1")
        @NotNull(message = "Quantity cannot be null")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Schema(description = "Unit price or total price of the product", example = "125.50")
        @NotNull(message = "Price cannot be null")
        @Positive(message = "Price must be a positive number")
        Double price
) {}
