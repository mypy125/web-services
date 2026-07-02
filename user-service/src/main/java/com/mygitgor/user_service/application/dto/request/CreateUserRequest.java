package com.mygitgor.user_service.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request package for creating a new user account")
public record CreateUserRequest(

        @Schema(description = "Unique electronic mail address of the user", example = "john.doe@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "First and last name of the user", example = "John Doe")
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Schema(description = "Contact phone number (digits only or international format)", example = "+37499112233")
        @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits")
        String phoneNumber,

        @Schema(description = "URL string or identifier for the hosted profile avatar image", example = "avatars/user-123.jpg")
        String profileImage,

        @Schema(description = "Security role assigned to the user profile", example = "ROLE_CUSTOMER")
        String role
) {}
