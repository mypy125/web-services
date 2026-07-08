package com.mygitgor.seller_service.infrastructure.client.fallback;

import com.mygitgor.seller_service.domain.model.Coupon;
import com.mygitgor.seller_service.infrastructure.client.exception.CouponServiceException;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CouponServiceFallback {

    public Mono<Coupon> createCouponFallback(Coupon coupon, Throwable t) {
        log.error("Fallback triggered for createCoupon for seller: {} due to: {}", coupon.getSellerId(), t.getMessage());
        return Mono.error(CouponServiceException.unavailable("CREATE_COUPON"));
    }

    public Flux<Coupon> getCouponsBySellerIdFallback(SellerId sellerId, int page, int size, Throwable t) {
        log.warn("Fallback triggered for getCouponsBySellerId for seller: {}. Returning empty list.", sellerId);
        return Flux.empty();
    }
}
