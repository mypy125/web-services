package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.request.UpdateSellerRequest;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.exception.SellerNotFoundException;
import com.mygitgor.seller_service.domain.model.shared.valueobject.*;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.port.incoming.SellerUseCase;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.domain.service.SellerDomainService;
import com.mygitgor.seller_service.infrastructure.kafka.producer.SellerEventProducer;
import com.mygitgor.seller_service.infrastructure.mapper.SellerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerApplicationService implements SellerUseCase {
    private final SellerRepositoryPort sellerRepository;
    private final SellerDomainService sellerDomainService;
    private final SellerEventProducer eventProducer;
    private final SellerMapper mapper;

    public Mono<Seller> getSellerById(SellerId sellerId) {
        log.debug("Getting seller by ID: {}", sellerId);
        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())));
    }

    public Mono<Seller> updateSeller(SellerId sellerId, UpdateSellerRequest req) {
        log.info("Updating seller: {}", sellerId);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .map(seller -> {
                    seller.updateProfile(
                            req.sellerName(),
                            req.displayName(),
                            req.mobile(),
                            req.phoneNumber(),
                            req.profileImage(),
                            req.coverImage()
                    );
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .doOnSuccess(seller -> eventProducer.sendSellerUpdatedEvent(seller).subscribe());
    }

    public Mono<Seller> verifySellerEmail(Email email) {
        log.info("Verifying seller email: {}", email);

        return sellerRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(email.toString())))
                .map(seller -> {
                    seller.verifyEmail();
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .doOnSuccess(seller -> eventProducer.sendEmailVerifiedEvent(seller).subscribe());
    }

    @Override
    public Mono<Seller> registerSeller(Email email, String sellerName, String mobile, BusinessDetails businessDetails, BankDetails bankDetails, Address pickupAddress) {
        return null;
    }

    @Override
    public Mono<Seller> verifyEmail(Email email) {
        return null;
    }

    public Mono<Seller> verifyBusiness(SellerId sellerId, String verifiedBy, String notes) {
        log.info("Verifying business for seller: {}", sellerId);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.getValue().toString())))
                .map(seller -> {
                    seller.verifyBusiness(verifiedBy, notes);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .doOnSuccess(seller -> eventProducer.sendBusinessVerifiedEvent(seller).subscribe());
    }

    @Override
    public Mono<Seller> verifyTaxInfo(SellerId sellerId, String verifiedBy) {
        return null;
    }

    @Override
    public Mono<Seller> rejectVerification(SellerId sellerId, String reason, String rejectedBy) {
        return null;
    }

    public Mono<Seller> activateSeller(SellerId sellerId, String activatedBy) {
        log.info("Activating seller: {}", sellerId);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> sellerDomainService.activateSeller(seller, activatedBy))
                .flatMap(sellerRepository::save)
                .doOnSuccess(seller -> eventProducer.sendSellerActivatedEvent(seller).subscribe());
    }

    public Mono<Seller> suspendSeller(SellerId sellerId, String reason, String suspendedBy) {
        log.info("Suspending seller: {}", sellerId);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> sellerDomainService.suspendSeller(seller, reason, suspendedBy))
                .flatMap(sellerRepository::save)
                .doOnSuccess(seller -> eventProducer.sendSellerSuspendedEvent(seller).subscribe());
    }

    public Mono<Seller> banSeller(SellerId sellerId, String reason, String bannedBy) {
        log.info("Banning seller: {}", sellerId);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> sellerDomainService.banSeller(seller, reason, bannedBy))
                .flatMap(sellerRepository::save)
                .doOnSuccess(seller -> eventProducer.sendSellerBannedEvent(seller).subscribe());
    }

    @Override
    public Mono<Seller> updateProfile(SellerId sellerId, String sellerName, String displayName, String mobile, String phoneNumber, String profileImage, String coverImage) {
        return null;
    }

    @Override
    public Mono<Seller> updateStore(SellerId sellerId, String storeName, String storeDescription, String storeTagline, String storeLogo, String storeBanner, String storeWebsite, String storeEmail, String storePhone) {
        return null;
    }

    @Override
    public Mono<Seller> updateSocialMediaLinks(SellerId sellerId, String socialMediaLinks) {
        return null;
    }

    @Override
    public Mono<Seller> updateBusinessDetails(SellerId sellerId, BusinessDetails newDetails) {
        return null;
    }

    @Override
    public Mono<Seller> updateBankDetails(SellerId sellerId, BankDetails newBankDetails) {
        return null;
    }

    @Override
    public Mono<Seller> updatePickupAddress(SellerId sellerId, Address newAddress) {
        return null;
    }

    @Override
    public Mono<Seller> updateReturnAddress(SellerId sellerId, Address newAddress) {
        return null;
    }

    @Override
    public Mono<Seller> addWarehouseAddress(SellerId sellerId, Address warehouseAddress) {
        return null;
    }

    @Override
    public Mono<Seller> removeWarehouseAddress(SellerId sellerId, Address warehouseAddress) {
        return null;
    }

    @Override
    public Mono<Seller> updateTaxInfo(SellerId sellerId, String gstNumber, String panNumber, String tinNumber, String businessRegistrationNumber) {
        return null;
    }

    @Override
    public Mono<Seller> updateCommissionRate(SellerId sellerId, Double commissionRate, String updatedBy) {
        return null;
    }

    @Override
    public Mono<Seller> updateCashbackRate(SellerId sellerId, Double cashbackRate) {
        return null;
    }

    @Override
    public Mono<Seller> updateShippingSettings(SellerId sellerId, Integer processingTimeDays, Integer shippingTimeDays, Double freeShippingThreshold, Double domesticShippingCost, Double internationalShippingCost) {
        return null;
    }

    @Override
    public Mono<Seller> updateAutoAcceptOrders(SellerId sellerId, boolean autoAcceptOrders) {
        return null;
    }

    @Override
    public Mono<Seller> updateAutoConfirmDelivery(SellerId sellerId, boolean autoConfirmDelivery) {
        return null;
    }

    @Override
    public Mono<Seller> updateBusinessHours(SellerId sellerId, BusinessHours businessHours) {
        return null;
    }

    @Override
    public Mono<Seller> updateOrderStats(SellerId sellerId, Double orderAmount, boolean isCancelled, boolean isRefunded) {
        return null;
    }

    @Override
    public Mono<Seller> updateProductStats(SellerId sellerId, Integer totalProducts, Integer totalActiveProducts, Integer totalOutOfStockProducts) {
        return null;
    }

    @Override
    public Mono<Seller> updateRating(SellerId sellerId, Integer rating) {
        return null;
    }

    @Override
    public Mono<Seller> updateFollowersCount(SellerId sellerId, Integer increment) {
        return null;
    }

    @Override
    public Mono<Seller> updateResponseTime(SellerId sellerId, Double hours) {
        return null;
    }

    @Override
    public Mono<Seller> updateLastActive(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Seller> updateLastLogin(SellerId sellerId, LocalDateTime lastLoginAt) {
        return null;
    }
}
