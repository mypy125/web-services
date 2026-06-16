package com.mygitgor.user_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "Integration event emitted when a user's product preferences and purchase history metrics are updated")
public record ProductStatsUpdatedEvent(

        @Schema(description = "Unique identifier of the user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "Total count of physical product units purchased by the user lifetime", example = "114")
        @NotNull(message = "Total products purchased count cannot be null")
        @PositiveOrZero(message = "Total products purchased must be zero or a positive number")
        Integer totalProductsPurchased,

        @Schema(description = "The commercial category from which the user buys most frequently", example = "Electronics")
        String mostPurchasedCategory,

        @Schema(description = "Identifier of the most frequently ordered product by this user", example = "prod-9921-xyz")
        String favoriteProductId,

        @Schema(description = "Display name of the most frequently ordered product", example = "Mechanical Keyboard")
        String favoriteProductName,

        @Schema(description = "Identifier of the product from the latest transaction", example = "prod-1102-abc")
        String lastPurchasedProductId,

        @Schema(description = "Category of the product from the latest transaction", example = "Peripherals")
        String lastPurchasedCategory,

        @Schema(description = "Metadata timestamp indicating exactly when this behavioral snapshot was captured", example = "2026-06-16T18:26:00")
        @NotNull(message = "Occurrence timestamp cannot be null")
        LocalDateTime occurredAt
) {}