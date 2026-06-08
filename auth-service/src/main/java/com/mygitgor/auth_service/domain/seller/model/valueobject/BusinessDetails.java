package com.mygitgor.auth_service.domain.seller.model.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
public record BusinessDetails(String businessName, Email businessEmail, String businessMobile, String businessAddress,
                              String logo, String banner, String registrationNumber, String taxId, String website,
                              String businessType, String description) {
    public BusinessDetails {
        if (businessName == null || businessName.isBlank()) {
            throw new DomainException("Business name is required");
        }
        if (businessEmail == null) {
            throw new DomainException("Business email is required");
        }
        if (businessMobile == null || businessMobile.isBlank()) {
            throw new DomainException("Business mobile is required");
        }
        if (businessAddress == null || businessAddress.isBlank()) {
            throw new DomainException("Business address is required");
        }

    }
}

