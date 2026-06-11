package com.mygitgor.user_service.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String profileImage;
    private String role;
    private boolean emailVerified;
    private String accountStatus;
    private String defaultAddressId;
    private String defaultPaymentMethodId;
    private Integer totalOrdersCount;
    private Double totalSpentAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime emailVerifiedAt;
}
