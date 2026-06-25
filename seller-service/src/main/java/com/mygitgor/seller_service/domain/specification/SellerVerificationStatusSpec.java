package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.valueobject.SellerVerificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerVerificationStatusSpec {

    public Mono<Boolean> isFullyVerified(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean isFullyVerified = seller.isFullyVerified();
        log.debug("Seller {} is fully verified: {}", seller.getEmail(), isFullyVerified);
        return Mono.just(isFullyVerified);
    }

    public Mono<Boolean> isPendingVerification(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean isPending = seller.isPendingVerification();
        log.debug("Seller {} is pending verification: {}", seller.getEmail(), isPending);
        return Mono.just(isPending);
    }

    public Mono<Boolean> isRejected(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean isRejected = seller.getVerificationStatus() == SellerVerificationStatus.REJECTED;
        log.debug("Seller {} is rejected: {}", seller.getEmail(), isRejected);
        return Mono.just(isRejected);
    }

    public Mono<Boolean> canSell(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        boolean canSell = seller.canSell();
        log.debug("Seller [{}] authorization to sell check: {}", seller.getEmail(), canSell);
        return Mono.just(canSell);
    }
}