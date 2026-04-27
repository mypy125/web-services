package com.mygitgor.auth_service.domain.seller.model.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class BusinessDetails {
    private final String businessName;
    private final Email businessEmail;
    private final String businessMobile;
    private final String businessAddress;
    private final String registrationNumber;
    private final String taxId;
    private final String website;
    private final String description;

    public BusinessDetails(String businessName, Email businessEmail, String businessMobile,
                           String businessAddress, String registrationNumber, String taxId,
                           String website, String description) {
        if (businessName == null || businessName.isBlank()) {
            throw new DomainException("Business name is required");
        }
        if (businessEmail == null) {
            throw new DomainException("Business email is required");
        }
        if (businessMobile == null || businessMobile.isBlank()) {
            throw new DomainException("Business mobile is required");
        }

        this.businessName = businessName;
        this.businessEmail = businessEmail;
        this.businessMobile = businessMobile;
        this.businessAddress = businessAddress;
        this.registrationNumber = registrationNumber;
        this.taxId = taxId;
        this.website = website;
        this.description = description;
    }
}

