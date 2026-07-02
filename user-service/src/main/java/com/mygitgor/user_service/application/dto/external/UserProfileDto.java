package com.mygitgor.user_service.application.dto.external;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileDto(
        String id,
        String email,
        String fullName,
        String phoneNumber,
        String profileImage,
        String role,
        boolean emailVerified,
        String accountStatus,
        boolean canLogin,
        boolean canPurchase,
        boolean needsEmailVerification,
        String language,
        String timezone,
        boolean newsletterSubscribed,
        boolean marketingConsent,
        String defaultAddressId,
        String defaultShippingAddressId,
        String defaultPaymentMethodId,
        Integer totalOrdersCount,
        Double totalSpentAmount,
        AddressDto defaultAddress,
        AddressDto defaultShippingAddress,
        PaymentMethodDto defaultPaymentMethod,
        List<OrderSummaryDto> recentOrders,
        CartSummaryDto currentCart,
        Integer loyaltyPoints,
        String loyaltyTier,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        LocalDateTime emailVerifiedAt,
        LocalDateTime lastPasswordChangeAt,
        boolean locked,
        LocalDateTime lockedUntil,
        String displayName
) {

    public boolean isActive() {
        return "ACTIVE".equals(accountStatus);
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }

    public boolean isSeller() {
        return "ROLE_SELLER".equals(role);
    }

    public UserProfileDto {
        if (displayName == null && fullName != null) {
            displayName = fullName;
        } else if (displayName == null) {
            displayName = email;
        }
    }
}