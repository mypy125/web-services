package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.valueobject.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerAddressSpec {

    public Mono<Boolean> isValidAddress(Address address) {
        if (address == null) {
            return Mono.just(false);
        }

        boolean isValid = address.addressLine1() != null && !address.addressLine1().isBlank()
                && address.city() != null && !address.city().isBlank()
                && address.state() != null && !address.state().isBlank()
                && address.postalCode() != null && !address.postalCode().isBlank()
                && address.country() != null && !address.country().isBlank();

        log.debug("Address is valid: {}", isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = postalCode.matches("^[0-9]{5,10}$");
        log.debug("Postal code {} is valid: {}", postalCode, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> hasPickupAddress(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean hasPickup = seller.getPickupAddress() != null;
        log.debug("Seller {} has pickup address: {}", seller.getEmail(), hasPickup);
        return Mono.just(hasPickup);
    }

    public Mono<Boolean> hasReturnAddress(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean hasReturn = seller.getReturnAddress() != null;
        log.debug("Seller {} has return address: {}", seller.getEmail(), hasReturn);
        return Mono.just(hasReturn);
    }
}