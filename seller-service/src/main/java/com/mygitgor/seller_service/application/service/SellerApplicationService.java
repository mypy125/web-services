package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.request.UpdateSellerRequest;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.shared.exception.SellerNotFoundException;
import com.mygitgor.seller_service.shared.valueobject.*;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.port.incoming.SellerUseCase;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.domain.service.SellerDomainService;
import com.mygitgor.seller_service.infrastructure.kafka.producer.SellerEventProducer;
import com.mygitgor.seller_service.infrastructure.mapper.SellerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerApplicationService implements SellerUseCase {
    private final SellerRepositoryPort sellerRepository;
    private final SellerDomainService sellerDomainService;
    private final SellerEventProducer eventProducer;

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
    @Transactional
    public Mono<Seller> registerSeller(Email email, String sellerName, String mobile, BusinessDetails businessDetails, BankDetails bankDetails, Address pickupAddress) {
        log.info("Registering new seller with email: {}", email);

        Seller newSeller = Seller.createPending(email, sellerName, mobile, businessDetails, bankDetails, pickupAddress);

        return sellerRepository.save(newSeller)
                .flatMap(seller -> eventProducer.sendSellerRegisteredEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> {
                            log.error("Failed to send SellerRegisteredEvent for seller: {}", seller.getSellerId(), err);
                            return Mono.empty();
                        })
                        .thenReturn(seller)
                );
    }

    @Override
    @Transactional
    public Mono<Seller> verifyEmail(Email email) {
        log.info("Verifying seller email: {}", email);

        return sellerRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(email.toString())))
                .map(seller -> {
                    seller.verifyEmail();
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendEmailVerifiedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller)
                );
    }

    @Override
    @Transactional
    public Mono<Seller> verifyBusiness(SellerId sellerId, String verifiedBy, String notes) {
        log.info("Verifying business for seller: {}", sellerId);

        return getSellerById(sellerId)
                .map(seller -> {
                    seller.verifyBusiness(verifiedBy, notes);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendBusinessVerifiedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller)
                );
    }

    @Override
    @Transactional
    public Mono<Seller> verifyTaxInfo(SellerId sellerId, String verifiedBy) {
        log.info("Verifying tax info for seller: {}, by: {}", sellerId, verifiedBy);
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.verifyTax(verifiedBy);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendTaxVerifiedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller)
                );
    }

    @Override
    @Transactional
    public Mono<Seller> rejectVerification(SellerId sellerId, String reason, String rejectedBy) {
        log.warn("Rejecting verification for seller: {}. Reason: {}", sellerId, reason);
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.rejectVerification(reason, rejectedBy);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendVerificationRejectedEvent(seller, seller.getRejectionReason())
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller)
                );
    }

    @Override
    @Transactional
    public Mono<Seller> activateSeller(SellerId sellerId, String activatedBy) {
        log.info("Activating seller: {}", sellerId);

        return getSellerById(sellerId)
                .flatMap(seller -> sellerDomainService.activateSeller(seller, activatedBy))
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendSellerActivatedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller)
                );
    }

    @Override
    @Transactional
    public Mono<Seller> suspendSeller(SellerId sellerId, String reason, String suspendedBy) {
        log.info("Suspending seller: {}", sellerId);

        return getSellerById(sellerId)
                .flatMap(seller -> sellerDomainService.suspendSeller(seller, reason, suspendedBy))
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendSellerSuspendedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller)
                );
    }

    @Override
    @Transactional
    public Mono<Seller> banSeller(SellerId sellerId, String reason, String bannedBy) {
        log.info("Banning seller: {}", sellerId);

        return getSellerById(sellerId)
                .flatMap(seller -> sellerDomainService.banSeller(seller, reason, bannedBy))
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendSellerBannedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller)
                );
    }

    @Override
    @Transactional
    public Mono<Seller> updateProfile(SellerId sellerId, String sellerName, String displayName, String mobile, String phoneNumber, String profileImage, String coverImage) {
        log.info("Updating profile for seller: {}", sellerId);
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateProfile(sellerName, displayName, mobile, phoneNumber, profileImage, coverImage);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendSellerUpdatedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller)
                );
    }

    @Override
    @Transactional
    public Mono<Seller> updateStore(SellerId sellerId, String storeName, String storeDescription, String storeTagline, String storeLogo, String storeBanner, String storeWebsite, String storeEmail, String storePhone) {
        log.info("Updating store configuration for seller: {}", sellerId);
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateStoreDetails(storeName, storeDescription, storeTagline, storeLogo, storeBanner, storeWebsite, storeEmail, storePhone);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateSocialMediaLinks(SellerId sellerId, String socialMediaLinks) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateSocialLinks(socialMediaLinks);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateBusinessDetails(SellerId sellerId, BusinessDetails newDetails) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateBusinessDetails(newDetails);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateBankDetails(SellerId sellerId, BankDetails newBankDetails) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateBankDetails(newBankDetails);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updatePickupAddress(SellerId sellerId, Address newAddress) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updatePickupAddress(newAddress);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateReturnAddress(SellerId sellerId, Address newAddress) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateReturnAddress(newAddress);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> addWarehouseAddress(SellerId sellerId, Address warehouseAddress) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.addWarehouse(warehouseAddress);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> removeWarehouseAddress(SellerId sellerId, Address warehouseAddress) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.removeWarehouse(warehouseAddress);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateTaxInfo(SellerId sellerId, String gstNumber, String panNumber, String tinNumber, String businessRegistrationNumber) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateTaxDetails(gstNumber, panNumber, tinNumber, businessRegistrationNumber);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateCommissionRate(SellerId sellerId, Double commissionRate, String updatedBy) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateCommission(commissionRate, updatedBy);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateCashbackRate(SellerId sellerId, Double cashbackRate) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateCashback(cashbackRate);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateShippingSettings(SellerId sellerId, Integer processingTimeDays, Integer shippingTimeDays, Double freeShippingThreshold, Double domesticShippingCost, Double internationalShippingCost) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateShipping(processingTimeDays, shippingTimeDays, freeShippingThreshold, domesticShippingCost, internationalShippingCost);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateAutoAcceptOrders(SellerId sellerId, boolean autoAcceptOrders) {
        return getSellerById(sellerId).map(s -> {
            s.setAutoAcceptOrders(autoAcceptOrders); return s;
        }).flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateAutoConfirmDelivery(SellerId sellerId, boolean autoConfirmDelivery) {
        return getSellerById(sellerId).map(s -> {
            s.setAutoConfirmDelivery(autoConfirmDelivery); return s;
        }).flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateBusinessHours(SellerId sellerId, BusinessHours businessHours) {
        return getSellerById(sellerId).map(s -> {
            s.updateBusinessHours(businessHours); return s;
        }).flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateOrderStats(SellerId sellerId, Double orderAmount, boolean isCancelled, boolean isRefunded) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.recalculateOrderStats(orderAmount, isCancelled, isRefunded);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateProductStats(SellerId sellerId, Integer totalProducts, Integer totalActiveProducts, Integer totalOutOfStockProducts) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.updateProductCounters(totalProducts, totalActiveProducts, totalOutOfStockProducts);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateRating(SellerId sellerId, Integer rating) {
        return getSellerById(sellerId).map(s -> {
            s.appendRating(rating); return s;
        }).flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateFollowersCount(SellerId sellerId, Integer increment) {
        return getSellerById(sellerId).map(s -> {
            s.changeFollowers(increment); return s;
        }).flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateResponseTime(SellerId sellerId, Double hours) {
        return getSellerById(sellerId).map(s -> {
            s.setResponseTimeHours(hours); return s;
        }).flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateLastActive(SellerId sellerId) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.setLastActiveAt(LocalDateTime.now());
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    @Override
    @Transactional
    public Mono<Seller> updateLastLogin(SellerId sellerId, LocalDateTime lastLoginAt) {
        return getSellerById(sellerId)
                .map(seller -> {
                    seller.setLastLoginAt(lastLoginAt);
                    seller.setLastActiveAt(lastLoginAt);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }
}
