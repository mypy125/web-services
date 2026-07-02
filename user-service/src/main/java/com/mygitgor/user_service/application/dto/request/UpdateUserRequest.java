package com.mygitgor.user_service.application.dto.request;

import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request package for comprehensive user profile and settings update")
public record UpdateUserRequest(

        @Schema(description = "Full name of the user", example = "Alexander Patterson")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Schema(description = "Contact phone number", example = "37499112233")
        @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits")
        String phoneNumber,

        @Schema(description = "URL or identifier of the user's avatar image", example = "avatars/user-99.png")
        String profileImage,

        @Schema(description = "Identifier of the default shipping address", example = "addr-8831-vbn")
        String defaultAddressId,

        @Schema(description = "Identifier of the default payment method", example = "pay-4412-qwe")
        String defaultPaymentMethodId
) {}