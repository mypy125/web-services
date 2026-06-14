package com.mygitgor.user_service.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {
    @Id
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private boolean emailVerified;
    private String profileImage;
    private String phoneNumber;
    private String accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime emailVerifiedAt;
    private String defaultAddressId;
    private String defaultPaymentMethodId;
    private Integer totalOrdersCount;
    private Double totalSpentAmount;
}
