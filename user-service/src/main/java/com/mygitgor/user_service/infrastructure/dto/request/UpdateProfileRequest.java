package com.mygitgor.user_service.infrastructure.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request package for updating user profile information")
public record UpdateProfileRequest(

        @Schema(description = "Updated full name of the user", example = "Johnathan Doe")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Schema(description = "Updated contact phone number in international format", example = "+37499112233")
        @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid phone number format")
        String phoneNumber,

        @Schema(description = "Updated URL or path to the user's avatar image", example = "avatars/new-profile.jpg")
        String profileImage
) {}
