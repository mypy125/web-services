package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.valueobject.BusinessDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerBusinessDetailsSpec {

    public Mono<Boolean> isBusinessDetailsComplete(Seller seller) {
        if (seller == null || seller.getBusinessDetails() == null) {
            return Mono.just(false);
        }

        BusinessDetails business = seller.getBusinessDetails();
        boolean isComplete = business.businessName() != null && !business.businessName().isBlank()
                && business.businessEmail() != null && !business.businessEmail().isBlank()
                && business.businessMobile() != null && !business.businessMobile().isBlank()
                && business.businessAddress() != null && !business.businessAddress().isBlank();

        log.debug("Seller {} business details are complete: {}", seller.getEmail(), isComplete);
        return Mono.just(isComplete);
    }

    public Mono<Boolean> canAddProducts(Seller seller) {
        return isBusinessDetailsComplete(seller)
                .flatMap(isComplete -> {
                    if (!isComplete) {
                        return Mono.just(false);
                    }
                    return Mono.just(seller.canSell());
                });
    }

    public Mono<Boolean> isValidRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = registrationNumber.matches("^[A-Z0-9]{8,15}$");
        log.debug("Registration number {} is valid: {}", registrationNumber, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidGstNumber(String gstNumber) {
        if (gstNumber == null || gstNumber.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = gstNumber.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$");
        log.debug("GST number {} is valid: {}", gstNumber, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidPanNumber(String panNumber) {
        if (panNumber == null || panNumber.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = panNumber.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
        log.debug("PAN number {} is valid: {}", panNumber, isValid);
        return Mono.just(isValid);
    }
}
