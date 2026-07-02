package com.mygitgor.user_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Response object containing detailed user loyalty program status and progression metrics")
public record LoyaltyInfoResponse(

        @Schema(description = "Unique identifier of the user", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "User's electronic mail address", example = "alex.patterson@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Total accumulated loyalty points currently available", example = "450")
        @NotNull(message = "Loyalty points cannot be null")
        @PositiveOrZero(message = "Loyalty points must be zero or a positive number")
        Integer loyaltyPoints,

        @Schema(description = "The name of the current loyalty tier", example = "GOLD")
        @NotBlank(message = "Loyalty tier cannot be blank")
        String loyaltyTier,

        @Schema(description = "Numeric representation of the current level", example = "3")
        @NotNull(message = "Current level cannot be null")
        @Min(value = 1, message = "Current level must be at least 1")
        Integer currentLevel,

        @Schema(description = "Total points required to reach the next milestone level", example = "500")
        @NotNull(message = "Next level points cannot be null")
        @Min(value = 1, message = "Next level points must be at least 1")
        Integer nextLevelPoints,

        @Schema(description = "Remaining points needed to cross into the next level", example = "50")
        @NotNull(message = "Points to next level cannot be null")
        @PositiveOrZero(message = "Points to next level must be zero or positive")
        Integer pointsToNextLevel,

        @Schema(description = "Percentage progress towards the next level (from 0.0 to 100.0)", example = "90.0")
        @NotNull(message = "Progress to next level cannot be null")
        @Min(value = 0, message = "Progress cannot be less than 0%")
        @Max(value = 100, message = "Progress cannot exceed 100%")
        Double progressToNextLevel,

        @Schema(description = "The multiplier or percentage rate for earning cashback", example = "0.05")
        @NotNull(message = "Cashback rate cannot be null")
        @PositiveOrZero(message = "Cashback rate must be zero or positive")
        Double cashbackRate,

        @Schema(description = "The immediate discount rate applied to purchases", example = "0.10")
        @NotNull(message = "Discount rate cannot be null")
        @PositiveOrZero(message = "Discount rate must be zero or positive")
        Double discountRate,

        @Schema(description = "A JSON or text description of active benefits for this tier", example = "Free shipping, 24/7 Priority support")
        String benefits
) {}
