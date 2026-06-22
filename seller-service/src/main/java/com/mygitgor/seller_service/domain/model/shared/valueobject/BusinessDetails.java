package com.mygitgor.seller_service.domain.model.shared.valueobject;

import lombok.Builder;

@Builder
public record BusinessDetails(
        String businessName,
        String businessEmail,
        String businessMobile,
        String businessAddress,
        String businessWebsite,
        String businessDescription,
        String businessType,
        Integer yearOfEstablishment,
        Integer numberOfEmployees,
        String registrationNumber,
        String taxId,
        String logo,
        String banner
) {}