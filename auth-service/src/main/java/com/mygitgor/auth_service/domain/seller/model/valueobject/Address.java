package com.mygitgor.auth_service.domain.seller.model.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class Address {
    private final String name;
    private final String locality;
    private final String address;
    private final String city;
    private final String state;
    private final String pinCode;
    private final String mobile;
    private final String addressType;
    private final boolean isDefault;

    public Address(String name, String locality, String address, String city,
                   String state, String pinCode, String mobile, String addressType, boolean isDefault) {
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

        this.name = name;
        this.locality = locality;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pinCode = pinCode;
        this.mobile = mobile;
        this.addressType = addressType;
        this.isDefault = isDefault;
    }
}
