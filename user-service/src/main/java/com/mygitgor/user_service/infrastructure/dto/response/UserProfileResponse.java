package com.mygitgor.user_service.infrastructure.dto.response;

import com.mygitgor.user_service.infrastructure.dto.external.AddressDto;
import com.mygitgor.user_service.infrastructure.dto.external.CouponDto;
import com.mygitgor.user_service.infrastructure.dto.external.OrderSummaryDto;
import com.mygitgor.user_service.infrastructure.dto.external.PaymentMethodDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Aggregated composite response representing the complete user profile dashboard view")
public record UserProfileResponse(

        @Schema(description = "Unique identifier of the user", example = "usr-4412-xyz")
        @NotBlank(message = "ID cannot be blank")
        String id,

        @Schema(description = "Primary electronic mail address", example = "alex.patterson@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Full display name of the user", example = "Alex Patterson")
        @NotBlank(message = "Full name cannot be blank")
        String fullName,

        @Schema(description = "Contact phone number", example = "+37499112233")
        String phoneNumber,

        @Schema(description = "URL or path to the user's avatar image", example = "avatars/user-99.jpg")
        String profileImage,

        @Schema(description = "Security role assigned to the user", example = "ROLE_CUSTOMER")
        @NotBlank(message = "Role cannot be blank")
        String role,

        @Schema(description = "Flag indicating whether the email address has been verified", example = "true")
        boolean emailVerified,

        @Schema(description = "Primary/default shipping address details")
        AddressDto defaultAddress,

        @Schema(description = "Primary/default billing and payment instrument details")
        PaymentMethodDto defaultPaymentMethod,

        @Schema(description = "List of valid active discount coupons available to the user")
        @NotNull(message = "Available coupons list cannot be null")
        List<CouponDto> availableCoupons,

        @Schema(description = "Lightweight overview of recent orders placed by the user")
        @NotNull(message = "Recent orders list cannot be null")
        List<OrderSummaryDto> recentOrders,

        @Schema(description = "Aggregated total number of completed orders", example = "24")
        @NotNull(message = "Total orders count cannot be null")
        @PositiveOrZero(message = "Total orders count must be zero or a positive number")
        Integer totalOrdersCount,

        @Schema(description = "Aggregated lifetime monetary expenditure in the system", example = "2450.75")
        @NotNull(message = "Total spent amount cannot be null")
        @PositiveOrZero(message = "Total spent amount must be zero or a positive number")
        Double totalSpentAmount,

        @Schema(description = "Calculated mathematical mean values of user check size", example = "102.11")
        @NotNull(message = "Average order value cannot be null")
        @PositiveOrZero(message = "Average order value must be zero or a positive number")
        Double averageOrderValue,

        @Schema(description = "Timestamp when the user account was originally created", example = "2026-01-15T10:00:00")
        @NotNull(message = "Registration timestamp cannot be null")
        LocalDateTime memberSince,

        @Schema(description = "Timestamp of the user's last successful authentication event", example = "2026-06-15T17:45:00")
        LocalDateTime lastLoginAt
) {
    public UserProfileResponse {
        availableCoupons = availableCoupons == null ? List.of() : List.copyOf(availableCoupons);
        recentOrders = recentOrders == null ? List.of() : List.copyOf(recentOrders);
    }
}
