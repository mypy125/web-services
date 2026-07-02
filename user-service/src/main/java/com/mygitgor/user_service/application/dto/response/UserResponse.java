package com.mygitgor.user_service.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard data transfer object representing core user details and metadata")
public record UserResponse(

        @Schema(description = "Unique identifier of the user", example = "usr-4412-xyz")
        @NotBlank(message = "ID cannot be blank")
        String id,

        @Schema(description = "Primary email address of the user", example = "john.doe@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Full legal or display name of the user", example = "John Doe")
        @NotBlank(message = "Full name cannot be blank")
        String fullName,

        @Schema(description = "Contact phone number", example = "+37499112233")
        String phoneNumber,

        @Schema(description = "URL path to the hosted avatar image", example = "avatars/user-123.jpg")
        String profileImage,

        @Schema(description = "Assigned security role string", example = "ROLE_CUSTOMER")
        @NotBlank(message = "Role cannot be blank")
        String role,

        @Schema(description = "Flag specifying if the email ownership is verified", example = "true")
        boolean emailVerified,

        @Schema(description = "Current account state within the platform life-cycle", example = "ACTIVE")
        @NotBlank(message = "Account status cannot be blank")
        String accountStatus,

        @Schema(description = "Identifier of the default shipping address record", example = "addr-7721-qwe")
        String defaultAddressId,

        @Schema(description = "Identifier of the default saved wallet payment instrument", example = "pay-3341-xyz")
        String defaultPaymentMethodId,

        @Schema(description = "Total number of non-cancelled orders made by this user", example = "12")
        @PositiveOrZero(message = "Total orders count must be zero or a positive number")
        Integer totalOrdersCount,

        @Schema(description = "Lifetime gross monetary value spent by the user", example = "850.25")
        @PositiveOrZero(message = "Total spent amount must be zero or a positive number")
        Double totalSpentAmount,

        @Schema(description = "Timestamp indicating when the account was registered", example = "2026-01-15T10:00:00")
        @NotNull(message = "Creation timestamp cannot be null")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of the last profile or state update", example = "2026-06-15T18:30:00")
        @NotNull(message = "Update timestamp cannot be null")
        LocalDateTime updatedAt,

        @Schema(description = "Timestamp of the last successful token generation or login", example = "2026-06-15T17:45:00")
        LocalDateTime lastLoginAt,

        @Schema(description = "Timestamp indicating exactly when the email flag was set to true", example = "2026-01-15T10:05:00")
        LocalDateTime emailVerifiedAt
) {}
