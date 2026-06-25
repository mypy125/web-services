package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
public class SellerCommissionSpec {
    private static final double DEFAULT_MIN_COMMISSION = 0.0;
    private static final double DEFAULT_MAX_COMMISSION = 50.0;
    private static final double MIN_CASHBACK = 0.0;
    private static final double MAX_CASHBACK = 10.0;

    public Mono<Boolean> isCommissionRateValid(Seller seller, Double newRate) {
        if (seller == null || newRate == null) {
            return Mono.just(false);
        }

        double min = Objects.requireNonNullElse(seller.getMinimumCommissionRate(), DEFAULT_MIN_COMMISSION);
        double max = Objects.requireNonNullElse(seller.getMaximumCommissionRate(), DEFAULT_MAX_COMMISSION);

        boolean isValid = newRate >= min && newRate <= max;
        log.debug("Commission rate {} is valid (min: {}, max: {}): {}", newRate, min, max, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> canUpdateCommission(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        boolean canUpdate = seller.canUpdateCommission();
        log.debug("Seller {} can update commission: {}", seller.getEmail(), canUpdate);
        return Mono.just(canUpdate);
    }

    public Mono<Boolean> isCashbackRateValid(Double cashbackRate) {
        if (cashbackRate == null) {
            return Mono.just(false);
        }
        boolean isValid = cashbackRate >= MIN_CASHBACK && cashbackRate <= MAX_CASHBACK;
        log.debug("Cashback rate {} is valid: {}", cashbackRate, isValid);
        return Mono.just(isValid);
    }
}
