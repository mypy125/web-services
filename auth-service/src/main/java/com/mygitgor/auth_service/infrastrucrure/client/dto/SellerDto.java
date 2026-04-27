package com.mygitgor.auth_service.infrastrucrure.client.dto;

import com.mygitgor.auth_service.application.dto.common.AddressDto;
import com.mygitgor.auth_service.application.dto.common.BankDetailsDto;
import com.mygitgor.auth_service.application.dto.common.BusinessDetailsDto;
import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.seller.model.SellerVerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerDto {
    private UUID id;
    private String sellerName;
    private String email;
    private String mobile;
    private UserRole role;
    private boolean emailVerified;
    private SellerVerificationStatus verificationStatus;
    private AccountStatus accountStatus;
    private BusinessDetailsDto businessDetails;
    private BankDetailsDto bankDetails;
    private AddressDto pickupAddress;
    private String gstNumber;
    private String panNumber;
    private Double commissionRate;
    private String storeLogo;
    private String storeBanner;
    private String storeDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime businessVerifiedAt;
    private LocalDateTime lastActiveAt;
}
