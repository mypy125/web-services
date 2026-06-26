package com.mygitgor.seller_service.domain.model.statistic;

import com.mygitgor.seller_service.domain.model.shared.valueobject.Address;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class AddressStatistics {
    private Long totalAddresses;
    private Long pickupAddressCount;
    private Long returnAddressCount;
    private Long warehouseAddressCount;
    private Long officeAddressCount;
    private Long shippingAddressCount;
    private Long billingAddressCount;
    private Long otherAddressCount;
    private Map<String, Long> addressesByCountry;
    private Map<String, Long> addressesByCity;
    private Map<String, Long> addressesByType;
    private Address defaultPickupAddress;
    private Address defaultReturnAddress;
    private Address defaultShippingAddress;
    private Address defaultBillingAddress;
    private LocalDateTime calculatedAt;
}
