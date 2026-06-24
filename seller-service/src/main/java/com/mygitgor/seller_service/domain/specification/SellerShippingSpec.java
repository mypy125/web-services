package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerShippingSpec {

    public Mono<Boolean> isValidProcessingTime(Integer processingTimeDays) {
        if (processingTimeDays == null) {
            return Mono.just(false);
        }
        boolean isValid = processingTimeDays >= 1 && processingTimeDays <= 30;
        log.debug("Processing time {} is valid: {}", processingTimeDays, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidShippingTime(Integer shippingTimeDays) {
        if (shippingTimeDays == null) {
            return Mono.just(false);
        }
        boolean isValid = shippingTimeDays >= 1 && shippingTimeDays <= 45;
        log.debug("Shipping time {} is valid: {}", shippingTimeDays, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidFreeShippingThreshold(Double threshold) {
        if (threshold == null) {
            return Mono.just(false);
        }
        boolean isValid = threshold >= 0;
        log.debug("Free shipping threshold {} is valid: {}", threshold, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidShippingCost(Double shippingCost) {
        if (shippingCost == null) {
            return Mono.just(false);
        }
        boolean isValid = shippingCost >= 0;
        log.debug("Shipping cost {} is valid: {}", shippingCost, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> canAutoAcceptOrders(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean canAutoAccept = seller.canSell() && seller.isAutoAcceptOrders();
        log.debug("Seller {} can auto accept orders: {}", seller.getEmail(), canAutoAccept);
        return Mono.just(canAutoAccept);
    }
}