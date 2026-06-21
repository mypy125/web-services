package com.mygitgor.user_service.infrastructure.dto.external;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record CartItemRequest(
        @NotBlank(message = "Product ID is required")
        String productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        String variantId
) {}
