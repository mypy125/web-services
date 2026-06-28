package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.shared.valueobject.BusinessDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

@Slf4j
@Component
public class SellerBusinessDetailsSpec {
    private static final String REGISTRATION_NUMBER_REGEX = "^[A-Z0-9]{8,15}$";
    private static final String GST_NUMBER_REGEX = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$";
    private static final String PAN_NUMBER_REGEX = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$";

    public Mono<Boolean> isBusinessDetailsComplete(Seller seller) {
        if (seller == null || seller.getBusinessDetails() == null) {
            return Mono.just(false);
        }

        BusinessDetails business = seller.getBusinessDetails();
        boolean isComplete = Stream.of(
                business.businessName(),
                business.businessEmail(),
                business.businessMobile(),
                business.businessAddress()
        ).allMatch(field -> field != null && !field.isBlank());

        log.debug("Seller {} business details are complete: {}", seller.getEmail(), isComplete);
        return Mono.just(isComplete);
    }

    public Mono<Boolean> canAddProducts(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        return isBusinessDetailsComplete(seller)
                .map(isComplete -> isComplete && seller.canSell());
    }

    public Mono<Boolean> isValidRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = registrationNumber.matches(REGISTRATION_NUMBER_REGEX);
        log.debug("Registration number {} is valid: {}", registrationNumber, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidGstNumber(String gstNumber) {
        if (gstNumber == null || gstNumber.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = gstNumber.matches(GST_NUMBER_REGEX);
        log.debug("GST number {} is valid: {}", gstNumber, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidPanNumber(String panNumber) {
        if (panNumber == null || panNumber.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = panNumber.matches(PAN_NUMBER_REGEX);
        log.debug("PAN number {} is valid: {}", panNumber, isValid);
        return Mono.just(isValid);
    }
}
