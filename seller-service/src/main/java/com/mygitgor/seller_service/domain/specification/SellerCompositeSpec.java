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
    private final SellerAccountStatusSpec accountStatusSpec;
    private final SellerBankDetailsSpec bankDetailsSpec;
    private final SellerBusinessDetailsSpec businessDetailsSpec;
    private final SellerCommissionSpec commissionSpec;

    public Mono<Boolean> isReadyToSell(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        return Mono.zip(
                verificationStatusSpec.isFullyVerified(seller),
                accountStatusSpec.isActive(seller),
                Mono.just(seller.isEmailVerified())
        ).map(tuple -> {
            boolean isReady = tuple.getT1() && tuple.getT2() && tuple.getT3();
            log.debug("Seller {} is ready to sell: {}", seller.getEmail(), isReady);
            return isReady;
        });
    }

    public Mono<Boolean> isFullyConfigured(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        return Mono.zip(
                businessDetailsSpec.isBusinessDetailsComplete(seller),
                bankDetailsSpec.isBankDetailsComplete(seller),
                accountStatusSpec.isActive(seller),
                verificationStatusSpec.isFullyVerified(seller),
                Mono.just(seller.isEmailVerified())
        ).map(tuple -> {
            boolean isConfigured = tuple.getT1() && tuple.getT2() && tuple.getT3()
                    && tuple.getT4() && tuple.getT5();
            log.debug("Seller {} is fully configured: {}", seller.getEmail(), isConfigured);
            return isConfigured;
        });
    }

    public Mono<Boolean> canReceivePayouts(Seller seller) {
        return bankDetailsSpec.canReceivePayouts(seller);
    }

    public Mono<Boolean> canAddProducts(Seller seller) {
        return businessDetailsSpec.canAddProducts(seller)
                .flatMap(canAdd -> {
                    if (!canAdd) {
                        return Mono.just(false);
                    }
                    return verificationStatusSpec.canSell(seller);
                });
    }
}
