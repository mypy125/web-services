package com.mygitgor.seller_service.domain.port.outgoing;

import com.mygitgor.seller_service.domain.model.Coupon;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CouponPort {
    Mono<Coupon> createCoupon(Coupon coupon);
    Flux<Coupon> getCouponsBySellerId(SellerId sellerId, int page, int size);
}
