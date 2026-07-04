package com.mygitgor.seller_service.presentation.controller;

import com.mygitgor.seller_service.application.dto.request.CreateSellerCouponRequest;
import com.mygitgor.seller_service.application.dto.response.CouponResponse;
import com.mygitgor.seller_service.application.service.SellerCouponService;
import com.mygitgor.seller_service.infrastructure.mapper.CouponMapper;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/coupons")
@RequiredArgsConstructor
public class SellerCouponController {
    private final SellerCouponService couponService;
    private final CouponMapper couponMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CouponResponse> createSellerCoupon(@PathVariable String sellerId,
                                                   @Valid @RequestBody CreateSellerCouponRequest request
    ) {

        log.info("REST request to create coupon for seller REST: {}", sellerId);
        return couponService.createSellerCoupon(new SellerId(sellerId), request)
                .map(couponMapper::toResponse);
    }

    @GetMapping
    public Flux<CouponResponse> getSellerCoupons(@PathVariable String sellerId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size
    ) {

        log.debug("REST request to get coupons page for seller: {}, page={}, size={}", sellerId, page, size);
        return couponService.getSellerCoupons(new SellerId(sellerId), page, size)
                .map(couponMapper::toResponse);
    }
}
