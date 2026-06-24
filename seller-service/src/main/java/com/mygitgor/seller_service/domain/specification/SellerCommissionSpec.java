package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerCommissionSpec {

    public Mono<Boolean> isCommissionRateValid(Seller seller, Double newRate) {
        if (seller == null || newRate == null) {
            return Mono.just(false);
        }

        double min = seller.getMinimumCommissionRate() != null ? seller.getMinimumCommissionRate() : 0.0;
        double max = seller.getMaximumCommissionRate() != null ? seller.getMaximumCommissionRate() : 50.0;

        boolean isValid = newRate >= min && newRate <= max;
        log.debug("Commission rate {} is valid (min: {}, max: {}): {}", newRate, min, max, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> canUpdateCommission(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        boolean canUpdate = seller.isActive() && seller.isFullyVerified();
        log.debug("Seller {} can update commission: {}", seller.getEmail(), canUpdate);
        return Mono.just(canUpdate);
    }

    public Mono<Boolean> isCashbackRateValid(Double cashbackRate) {
        if (cashbackRate == null) {
            return Mono.just(false);
        }
        boolean isValid = cashbackRate >= 0 && cashbackRate <= 10;
        log.debug("Cashback rate {} is valid: {}", cashbackRate, isValid);
        return Mono.just(isValid);
    }
}
