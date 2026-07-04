package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.model.type.AddressType;
import com.mygitgor.seller_service.domain.repository.AddressRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressUniquenessSpec {
    private final AddressRepositoryPort addressRepository;

    public Mono<Boolean> isAddressUnique(SellerId sellerId, Address address) {
        if (sellerId == null || address == null) {
            log.debug("SellerId or address is null");
            return Mono.just(false);
        }

        return addressRepository.findBySellerId(sellerId)
                .any(existingAddress ->
                        existingAddress.addressLine1().equals(address.addressLine1()) &&
                                existingAddress.city().equals(address.city()) &&
                                existingAddress.postalCode().equals(address.postalCode()) &&
                                existingAddress.country().equals(address.country())
                )
                .map(exists -> !exists)
                .doOnSuccess(isUnique -> log.debug("Address is unique: {}", isUnique));
    }

    public Mono<Boolean> isAddressUniqueForUpdate(SellerId sellerId, String addressId, Address address) {
        if (sellerId == null || addressId == null || address == null) {
            log.debug("SellerId, addressId or address is null");
            return Mono.just(false);
        }

        return addressRepository.findBySellerId(sellerId)
                .filter(existingAddress -> !existingAddress.id().equals(addressId))
                .any(existingAddress ->
                        existingAddress.addressLine1().equals(address.addressLine1()) &&
                                existingAddress.city().equals(address.city()) &&
                                existingAddress.postalCode().equals(address.postalCode()) &&
                                existingAddress.country().equals(address.country())
                )
                .map(exists -> !exists)
                .doOnSuccess(isUnique -> log.debug("Address is unique for update: {}", isUnique));
    }

    public Mono<Boolean> hasAddressOfType(SellerId sellerId, AddressType addressType) {
        if (sellerId == null || addressType == null) {
            log.debug("SellerId or addressType is null");
            return Mono.just(false);
        }

        return addressRepository.existsBySellerIdAndType(sellerId, addressType)
                .doOnSuccess(exists -> log.debug("Seller {} has address of type {}: {}", sellerId, addressType, exists));
    }

    public Mono<Boolean> hasDefaultAddressOfType(SellerId sellerId, AddressType addressType) {
        if (sellerId == null || addressType == null) {
            log.debug("SellerId or addressType is null");
            return Mono.just(false);
        }

        return addressRepository.findDefaultAddressBySellerIdAndType(sellerId, addressType)
                .hasElement()
                .doOnSuccess(exists -> log.debug("Seller {} has default address of type {}: {}", sellerId, addressType, exists));
    }
}
