package com.mygitgor.seller_service.infrastructure.client;

import com.mygitgor.seller_service.domain.model.Coupon;
import com.mygitgor.seller_service.domain.port.outgoing.CouponPort;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CouponServiceClient implements CouponPort {
    @Override
    public Mono<Coupon> createCoupon(Coupon coupon) {
        return null;
    }

    @Override
    public Flux<Coupon> getCouponsBySellerId(SellerId sellerId, int page, int size) {
        return null;
    }
}
