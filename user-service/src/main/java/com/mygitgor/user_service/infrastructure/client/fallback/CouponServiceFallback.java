package com.mygitgor.user_service.infrastructure.client.fallback;

import com.mygitgor.user_service.application.dto.external.CouponDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@Component
public class CouponServiceFallback {
    public Flux<CouponDto> getUserCoupons(String userId) {
        log.warn("Fallback: Returning empty coupons list for user: {}", userId);
        return Flux.empty();
    }

    public Flux<CouponDto> getUserCouponsEmptyList(String userId) {
        log.warn("Fallback: Returning empty list for user: {}", userId);
        return Flux.fromIterable(Collections.emptyList());
    }

    public Flux<CouponDto> getAvailableCoupons(String userId) {
        log.warn("Fallback: Returning empty available coupons for user: {}", userId);
        return Flux.empty();
    }

    public Mono<Void> markCouponAsUsed(String userId, String couponCode) {
        log.warn("Fallback: Could not mark coupon {} as used for user: {}", couponCode, userId);
        return Mono.empty();
    }

    public Mono<Boolean> validateCoupon(String userId, String couponCode) {
        log.warn("Fallback: Coupon validation failed for user: {}, coupon: {}", userId, couponCode);
        return Mono.just(false);
    }

    public Mono<Double> getCouponDiscount(String userId, String couponCode) {
        log.warn("Fallback: Returning 0 discount for user: {}, coupon: {}", userId, couponCode);
        return Mono.just(0.0);
    }

    public Mono<Long> countUserCoupons(String userId) {
        log.warn("Fallback: Returning 0 coupons count for user: {}", userId);
        return Mono.just(0L);
    }

    public Flux<CouponDto> getUserCouponsPaginated(String userId, int page, int size) {
        log.warn("Fallback: Returning empty coupons for user: {}, page: {}, size: {}", userId, page, size);
        return Flux.empty();
    }
}
