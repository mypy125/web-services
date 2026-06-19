package com.mygitgor.user_service.infrastructure.persistence.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Table("users")
public record UserEntity(
        @Id UUID id,
        String email,
        String fullName,
        String role,
        boolean emailVerified,
        String profileImage,
        String phoneNumber,
        String accountStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        LocalDateTime emailVerifiedAt,
        String defaultAddressId,
        String defaultPaymentMethodId,
        String defaultShippingAddressId,
        Integer totalOrdersCount,
        Double totalSpentAmount,
        String language,
        String timezone,
        boolean newsletterSubscribed,
        boolean marketingConsent,
        LocalDateTime lastPasswordChangeAt,
        Integer failedLoginAttempts,
        LocalDateTime lockedUntil
) {}
