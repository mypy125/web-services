package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.infrastructure.dto.external.CouponDto;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface CouponPort {
    Flux<CouponDto> getUserCoupons(String userId);
    Mono<Void> markCouponAsUsed(String userId, String couponCode);
}
