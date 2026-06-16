package com.mygitgor.user_service.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Request to update user loyalty points")
public record UpdateLoyaltyPointsRequest(

        @Schema(description = "The number of loyalty points to add or set", example = "150")
        @NotNull(message = "Loyalty points cannot be null")
        @PositiveOrZero(message = "Loyalty points must be zero or a positive number")
        Integer points
) {}
