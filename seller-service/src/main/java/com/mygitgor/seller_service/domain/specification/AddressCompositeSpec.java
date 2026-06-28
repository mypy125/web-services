package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.type.AddressType;
import com.mygitgor.seller_service.domain.repository.AddressRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressCompositeSpec {
    private final AddressValidationSpec validationSpec;
    private final AddressUniquenessSpec uniquenessSpec;
    private final AddressRepositoryPort repositoryPort;

    public Mono<Boolean> validateAddressCompletely(Address address) {
        if (address == null) {
            log.debug("Address is null");
            return Mono.just(false);
        }

        return validationSpec.isValidAddress(address)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.just(false);
                    }
                    return validationSpec.isValidPostalCode(address.postalCode())
                            .flatMap(isPostalValid -> {
                                if (!isPostalValid) {
                                    return Mono.just(false);
                                }
                                return validationSpec.isValidPhoneNumber(address.phoneNumber())
                                        .flatMap(isPhoneValid -> {
                                            if (!isPhoneValid) {
                                                return Mono.just(false);
                                            }
                                            return validationSpec.isValidCoordinates(
                                                    address.latitude(),
                                                    address.longitude()
                                            );
                                        });
                            });
                })
                .doOnSuccess(isValid -> log.debug("Address completely validated: {}", isValid));
    }

    public Mono<Boolean> validateAddressForCreation(SellerId sellerId, Address address) {
        return validateAddressCompletely(address)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.just(false);
                    }
                    return uniquenessSpec.isAddressUnique(sellerId, address);
                })
                .doOnSuccess(isValid -> log.debug("Address is valid for creation: {}", isValid));
    }

    public Mono<Boolean> validateAddressByType(Address address, AddressType expectedType) {
        return validationSpec.isCorrectAddressType(address, expectedType)
                .flatMap(isCorrect -> {
                    if (!isCorrect) {
                        return Mono.just(false);
                    }
                    return validateAddressCompletely(address);
                })
                .doOnSuccess(isValid -> log.debug("Address validated for type {}: {}", expectedType, isValid));
    }

    public Mono<Boolean> canAddAddressOfType(SellerId sellerId, AddressType addressType) {
        if (addressType == AddressType.PICKUP || addressType == AddressType.RETURN) {
            return uniquenessSpec.hasAddressOfType(sellerId, addressType)
                    .map(hasAddress -> !hasAddress)
                    .doOnSuccess(canAdd -> log.debug("Can add {} address: {}", addressType, canAdd));
        }
        return Mono.just(true);
    }

    public Mono<Boolean> hasAllRequiredAddresses(SellerId sellerId) {
        return Mono.zip(
                        uniquenessSpec.hasAddressOfType(sellerId, AddressType.PICKUP),
                        uniquenessSpec.hasAddressOfType(sellerId, AddressType.RETURN)
                ).map(tuple -> tuple.getT1() && tuple.getT2())
                .doOnSuccess(hasAll -> log.debug("Seller has all required addresses: {}", hasAll));
    }

    public Mono<Boolean> canAddMoreWarehouseAddresses(SellerId sellerId, int maxLimit) {
        return repositoryPort.countWarehouseAddressesBySellerId(sellerId)
                .map(count -> count < maxLimit)
                .doOnSuccess(canAdd -> log.debug("Can add more warehouse addresses: {}", canAdd));
    }
}
