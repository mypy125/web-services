package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.valueobject.Address;
import com.mygitgor.seller_service.domain.model.shared.valueobject.type.AddressType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

@Slf4j
@Component
public class SellerAddressSpec {
    private static final String POSTAL_CODE_REGEX = "^[0-9]{5,10}$";

    public Mono<Boolean> isValidAddress(Address address) {
        if (address == null) {
            return Mono.just(false);
        }

        boolean isValid = Stream.of(
                address.addressLine1(),
                address.city(),
                address.state(),
                address.postalCode(),
                address.country()
        ).allMatch(field -> field != null && !field.isBlank());

        log.debug("Address is valid: {}", isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = postalCode.matches(POSTAL_CODE_REGEX);
        log.debug("Postal code {} is valid: {}", postalCode, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> hasPickupAddress(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean hasPickup = seller.getPickupAddress().addressType() == AddressType.PICKUP;
        log.debug("Seller {} has pickup address: {}", seller.getEmail(), hasPickup);
        return Mono.just(hasPickup);
    }

    public Mono<Boolean> hasReturnAddress(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean hasReturn = seller.getReturnAddress().addressType() == AddressType.RETURN;
        log.debug("Seller {} has return address: {}", seller.getEmail(), hasReturn);
        return Mono.just(hasReturn);
    }
}