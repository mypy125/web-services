package com.mygitgor.auth_service.infrastrucrure.client.dto;

import com.mygitgor.auth_service.application.dto.common.AddressDto;
import com.mygitgor.auth_service.application.dto.common.BankDetailsDto;
import com.mygitgor.auth_service.application.dto.common.BusinessDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerCreateRequestDto {
    private String sellerName;
    private String email;
    private String mobile;
    private BusinessDetailsDto businessDetails;
    private BankDetailsDto bankDetails;
    private AddressDto pickupAddress;
    private String gstNumber;
    private String panNumber;
    private String storeLogo;
    private String storeBanner;
    private String storeDescription;
}
