package com.mygitgor.seller_service.domain.model.shared.valueobject;

import com.mygitgor.seller_service.domain.model.shared.valueobject.type.AddressType;
import lombok.Builder;

@Builder
public record Address(
        String id,
        String name,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        String phoneNumber,
        String landmark,
        Double latitude,
        Double longitude,
        AddressType addressType
) {}
