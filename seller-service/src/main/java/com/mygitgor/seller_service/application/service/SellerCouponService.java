package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.request.CreateSellerCouponRequest;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.port.outgoing.CouponPort;
import com.mygitgor.seller_service.domain.port.outgoing.ProductPort;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.shared.exception.DomainException;
import com.mygitgor.seller_service.shared.exception.SellerNotFoundException;
import com.mygitgor.seller_service.domain.model.Coupon;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerCouponService {
    private final CouponPort couponPort;
    private final SellerRepositoryPort sellerRepository;
    private final ProductPort productPort;

    public Mono<Coupon> createSellerCoupon(SellerId sellerId, CreateSellerCouponRequest request) {
        log.info("Creating coupon for seller: {}", sellerId);

        return validateSellerCanCreateCoupon(sellerId)
                .then(validateProductsBelongToSeller(sellerId, request.applicableProductIds()))
                .then(Mono.fromCallable(() -> Coupon.createNewSellerCoupon(
                        sellerId,
                        request.code(),
                        request.discountType(),
                        request.discountValue(),
                        request.minOrderAmount(),
                        request.maxDiscountAmount(),
                        request.validFrom(),
                        request.validUntil(),
                        request.usageLimit(),
                        request.applicableProductIds(),
                        request.applicableCategoryIds()
                )))
                .flatMap(couponPort::createCoupon);
    }

    public Flux<Coupon> getSellerCoupons(SellerId sellerId, int page, int size) {
        return couponPort.getCouponsBySellerId(sellerId, page, size);
    }

    private Mono<Seller> validateSellerCanCreateCoupon(SellerId sellerId) {
        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> {
                    if (!seller.canSell()) {
                        return Mono.error(new DomainException("Seller account is not active or verified to create coupons"));
                    }
                    return Mono.just(seller);
                });
    }

    private Mono<Void> validateProductsBelongToSeller(SellerId sellerId, List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(productIds)
                .flatMap(productId -> productPort.isProductBelongsToSeller(new ProductId(productId), sellerId)
                        .switchIfEmpty(Mono.just(false))
                )
                .all(belongs -> belongs)
                .flatMap(allBelong -> {
                    if (!allBelong) {
                        return Mono.error(new DomainException("Validation failed: Some products do not belong to seller " + sellerId));
                    }
                    return Mono.empty();
                });
    }

}