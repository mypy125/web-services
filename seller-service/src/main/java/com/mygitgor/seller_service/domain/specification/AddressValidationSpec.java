package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.domain.model.type.AddressType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Slf4j
@Component
public class AddressValidationSpec {
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("^[0-9]{5,10}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9][0-9]{7,14}$");
    private static final Pattern LATITUDE_PATTERN = Pattern.compile("^-?([0-8]?[0-9])\\.\\d+$|^-?90\\.0+$");
    private static final Pattern LONGITUDE_PATTERN = Pattern.compile("^-?((1[0-7][0-9])|([0-9]?[0-9]))\\.\\d+$|^-?180\\.0+$");

    public Mono<Boolean> isValidAddress(Address address) {
        if (address == null) {
            log.debug("Address is null");
            return Mono.just(false);
        }

        boolean isValid = address.addressLine1() != null && !address.addressLine1().isBlank()
                && address.city() != null && !address.city().isBlank()
                && address.state() != null && !address.state().isBlank()
                && address.postalCode() != null && !address.postalCode().isBlank()
                && address.country() != null && !address.country().isBlank()
                && address.addressType() != null;

        log.debug("Address is valid: {}", isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            log.debug("Postal code is null or empty");
            return Mono.just(false);
        }
        boolean isValid = POSTAL_CODE_PATTERN.matcher(postalCode).matches();
        log.debug("Postal code {} is valid: {}", postalCode, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.debug("Phone number is null or empty");
            return Mono.just(true);
        }
        boolean isValid = PHONE_PATTERN.matcher(phoneNumber).matches();
        log.debug("Phone number {} is valid: {}", phoneNumber, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            log.debug("Coordinates are null");
            return Mono.just(true);
        }

        boolean isValid = latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        log.debug("Coordinates ({}, {}) are valid: {}", latitude, longitude, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isCorrectAddressType(Address address, AddressType expectedType) {
        if (address == null || address.addressType() == null || expectedType == null) {
            log.debug("Address or address type is null");
            return Mono.just(false);
        }
        boolean isCorrect = address.addressType() == expectedType;
        log.debug("Address type {} matches expected {}: {}", address.addressType(), expectedType, isCorrect);
        return Mono.just(isCorrect);
    }

    public Mono<Boolean> isCompleteAddress(Address address) {
        return isValidAddress(address)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.just(false);
                    }
                    return isValidPhoneNumber(address.phoneNumber())
                            .flatMap(isPhoneValid -> isValidCoordinates(address.latitude(), address.latitude())
                                    .map(isCoordsValid -> isPhoneValid && isCoordsValid));
                });
    }
}
