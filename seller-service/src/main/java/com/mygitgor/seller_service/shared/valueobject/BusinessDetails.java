package com.mygitgor.seller_service.shared.valueobject;

import com.mygitgor.seller_service.shared.valueobject.type.BusinessType;
import lombok.Builder;

@Builder
public record BusinessDetails(
        String businessName,
        String businessEmail,
        String businessMobile,
        String businessAddress,
        String businessWebsite,
        String businessDescription,
        BusinessType businessType,
        Integer yearOfEstablishment,
        Integer numberOfEmployees,
        String registrationNumber,
        String taxId,
        String logo,
        String banner
) {}