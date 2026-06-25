package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerCompositeSpec {
    private final SellerVerificationStatusSpec verificationStatusSpec;
    private final SellerBankDetailsSpec bankDetailsSpec;
    private final SellerBusinessDetailsSpec businessDetailsSpec;

    public Mono<Boolean> isReadyToSell(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        boolean isReady = seller.canSell();
        log.debug("Seller [{}] readiness to sell check: {}", seller.getEmail(), isReady);
        return Mono.just(isReady);
    }

    public Mono<Boolean> isFullyConfigured(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        return Mono.zip(
                businessDetailsSpec.isBusinessDetailsComplete(seller),
                bankDetailsSpec.isBankDetailsComplete(seller),
                isReadyToSell(seller)
        ).map(tuple -> {
            boolean isConfigured = tuple.getT1() && tuple.getT2() && tuple.getT3();
            log.debug("Seller [{}] full configuration completeness check: {}", seller.getEmail(), isConfigured);
            return isConfigured;
        });
    }

    public Mono<Boolean> canReceivePayouts(Seller seller) {
        return bankDetailsSpec.canReceivePayouts(seller);
    }

    public Mono<Boolean> canAddProducts(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        return businessDetailsSpec.canAddProducts(seller)
                .flatMap(canAdd -> canAdd ? verificationStatusSpec.canSell(seller) : Mono.just(false));
    }
}
