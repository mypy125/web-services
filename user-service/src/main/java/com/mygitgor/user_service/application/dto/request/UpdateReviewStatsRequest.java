package com.mygitgor.user_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request package to update user's review and rating statistics")
public record UpdateReviewStatsRequest(

        @Schema(description = "The rating score given by the user (usually 1 to 5)", example = "5")
        @NotNull(message = "Rating cannot be null")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        Integer rating,

        @Schema(description = "Unique identifier of the reviewed product", example = "prod-7711-abc")
        @NotBlank(message = "Product ID cannot be blank")
        String productId,

        @Schema(description = "Unique identifier of the created review", example = "rev-9942-lmn")
        @NotBlank(message = "Review ID cannot be blank")
        String reviewId
) {}
