package com.mygitgor.auth_service.domain.seller.model.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
public record Address(String name, String locality, String address, String city, String state, String pinCode,
                      String mobile, String addressType, boolean isDefault) {
    public Address {
        if (name == null || name.isBlank()) {
            throw new DomainException("Address name is required");
        }
        if (address == null || address.isBlank()) {
            throw new DomainException("Address is required");
        }
        if (city == null || city.isBlank()) {
            throw new DomainException("City is required");
        }
        if (state == null || state.isBlank()) {
            throw new DomainException("State is required");
        }
        if (pinCode == null || pinCode.isBlank()) {
            throw new DomainException("Pin code is required");
        }

    }
}
