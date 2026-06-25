package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerShippingSpec {
    private static final int MIN_PROCESSING_DAYS = 1;
    private static final int MAX_PROCESSING_DAYS = 30;
    private static final int MIN_SHIPPING_DAYS = 1;
    private static final int MAX_SHIPPING_DAYS = 45;
    private static final double MIN_COST_THRESHOLD = 0.0;

    public Mono<Boolean> isValidProcessingTime(Integer processingTimeDays) {
        if (processingTimeDays == null) {
            return Mono.just(false);
        }
        boolean isValid = processingTimeDays >= MIN_PROCESSING_DAYS && processingTimeDays <= MAX_PROCESSING_DAYS;
        log.debug("Processing time {} is valid: {}", processingTimeDays, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidShippingTime(Integer shippingTimeDays) {
        if (shippingTimeDays == null) {
            return Mono.just(false);
        }
        boolean isValid = shippingTimeDays >= MIN_SHIPPING_DAYS && shippingTimeDays <= MAX_SHIPPING_DAYS;
        log.debug("Shipping time {} is valid: {}", shippingTimeDays, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidFreeShippingThreshold(Double threshold) {
        if (threshold == null) {
            return Mono.just(false);
        }
        boolean isValid = threshold >= MIN_COST_THRESHOLD;
        log.debug("Free shipping threshold {} is valid: {}", threshold, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidShippingCost(Double shippingCost) {
        if (shippingCost == null) {
            return Mono.just(false);
        }
        boolean isValid = shippingCost >= MIN_COST_THRESHOLD;
        log.debug("Shipping cost {} is valid: {}", shippingCost, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> canAutoAcceptOrders(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean canAutoAccept = seller.canAcceptOrders();
        log.debug("Seller {} can auto accept orders: {}", seller.getEmail(), canAutoAccept);
        return Mono.just(canAutoAccept);
    }
}