package com.mygitgor.user_service.infrastructure.dto.response;

import com.mygitgor.user_service.infrastructure.dto.external.AddressDto;
import com.mygitgor.user_service.infrastructure.dto.external.CouponDto;
import com.mygitgor.user_service.infrastructure.dto.external.OrderSummaryDto;
import com.mygitgor.user_service.infrastructure.dto.external.PaymentMethodDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String profileImage;
    private String role;
    private boolean emailVerified;

    private AddressDto defaultAddress;
    private PaymentMethodDto defaultPaymentMethod;
    private List<CouponDto> availableCoupons;
    private List<OrderSummaryDto> recentOrders;

    private Integer totalOrdersCount;
    private Double totalSpentAmount;
    private Double averageOrderValue;

    private LocalDateTime memberSince;
    private LocalDateTime lastLoginAt;
}
