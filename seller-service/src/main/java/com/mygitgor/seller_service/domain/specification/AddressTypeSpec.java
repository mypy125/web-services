package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.shared.valueobject.Address;
import com.mygitgor.seller_service.domain.model.shared.valueobject.type.AddressType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AddressTypeSpec {

    public Mono<Boolean> isValidAddressType(String addressType) {
        if (addressType == null || addressType.isBlank()) {
            log.debug("Address type is null or empty");
            return Mono.just(false);
        }
        try {
            AddressType.valueOf(addressType.toUpperCase());
            log.debug("Address type {} is valid", addressType);
            return Mono.just(true);
        } catch (IllegalArgumentException e) {
            log.debug("Address type {} is invalid", addressType);
            return Mono.just(false);
        }
    }

    public Mono<Boolean> isPickupAddress(AddressType addressType) {
        boolean isPickup = addressType == AddressType.PICKUP;
        log.debug("Address type {} is PICKUP: {}", addressType, isPickup);
        return Mono.just(isPickup);
    }

    public Mono<Boolean> isReturnAddress(AddressType addressType) {
        boolean isReturn = addressType == AddressType.RETURN;
        log.debug("Address type {} is RETURN: {}", addressType, isReturn);
        return Mono.just(isReturn);
    }

    public Mono<Boolean> isWarehouseAddress(AddressType addressType) {
        boolean isWarehouse = addressType == AddressType.WAREHOUSE;
        log.debug("Address type {} is WAREHOUSE: {}", addressType, isWarehouse);
        return Mono.just(isWarehouse);
    }

    public Mono<Boolean> isOfficeAddress(AddressType addressType) {
        boolean isOffice = addressType == AddressType.OFFICE ||
                addressType == AddressType.REGISTERED_OFFICE ||
                addressType == AddressType.BRANCH_OFFICE;
        log.debug("Address type {} is OFFICE: {}", addressType, isOffice);
        return Mono.just(isOffice);
    }

    public Mono<Boolean> isBusinessAddress(AddressType addressType) {
        boolean isBusiness = addressType == AddressType.REGISTERED_OFFICE ||
                addressType == AddressType.BRANCH_OFFICE ||
                addressType == AddressType.STORE ||
                addressType == AddressType.SHOWROOM;
        log.debug("Address type {} is BUSINESS: {}", addressType, isBusiness);
        return Mono.just(isBusiness);
    }

    public Mono<Boolean> isDeliveryAddress(AddressType addressType) {
        boolean isDelivery = addressType == AddressType.SHIPPING ||
                addressType == AddressType.BILLING;
        log.debug("Address type {} is DELIVERY: {}", addressType, isDelivery);
        return Mono.just(isDelivery);
    }

}
