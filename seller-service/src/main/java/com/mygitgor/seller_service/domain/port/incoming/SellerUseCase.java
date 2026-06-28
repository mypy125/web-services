package com.mygitgor.seller_service.domain.port.incoming;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.shared.valueobject.*;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface SellerUseCase {
    Mono<Seller> registerSeller(
            Email email,
            String sellerName,
            String mobile,
            BusinessDetails businessDetails,
            BankDetails bankDetails,
            Address pickupAddress
    );
    Mono<Seller> verifyEmail(Email email);
    Mono<Seller> verifyBusiness(SellerId sellerId, String verifiedBy, String notes);
    Mono<Seller> verifyTaxInfo(SellerId sellerId, String verifiedBy);
    Mono<Seller> rejectVerification(SellerId sellerId, String reason, String rejectedBy);
    Mono<Seller> activateSeller(SellerId sellerId, String activatedBy);
    Mono<Seller> suspendSeller(SellerId sellerId, String reason, String suspendedBy);
    Mono<Seller> banSeller(SellerId sellerId, String reason, String bannedBy);
    Mono<Seller> updateProfile(
            SellerId sellerId,
            String sellerName,
            String displayName,
            String mobile,
            String phoneNumber,
            String profileImage,
            String coverImage
    );
    Mono<Seller> updateStore(
            SellerId sellerId,
            String storeName,
            String storeDescription,
            String storeTagline,
            String storeLogo,
            String storeBanner,
            String storeWebsite,
            String storeEmail,
            String storePhone
    );
    Mono<Seller> updateSocialMediaLinks(SellerId sellerId, String socialMediaLinks);
    Mono<Seller> updateBusinessDetails(SellerId sellerId, BusinessDetails newDetails);
    Mono<Seller> updateBankDetails(SellerId sellerId, BankDetails newBankDetails);
    Mono<Seller> updatePickupAddress(
            SellerId sellerId,
            Address newAddress
    );
    Mono<Seller> updateReturnAddress(
            SellerId sellerId,
            Address newAddress
    );
    Mono<Seller> addWarehouseAddress(
            SellerId sellerId,
            Address warehouseAddress
    );
    Mono<Seller> removeWarehouseAddress(
            SellerId sellerId,
            Address warehouseAddress
    );
    Mono<Seller> updateTaxInfo(
            SellerId sellerId,
            String gstNumber,
            String panNumber,
            String tinNumber,
            String businessRegistrationNumber
    );
    Mono<Seller> updateCommissionRate(SellerId sellerId, Double commissionRate, String updatedBy);
    Mono<Seller> updateCashbackRate(SellerId sellerId, Double cashbackRate);
    Mono<Seller> updateShippingSettings(
            SellerId sellerId,
            Integer processingTimeDays,
            Integer shippingTimeDays,
            Double freeShippingThreshold,
            Double domesticShippingCost,
            Double internationalShippingCost
    );
    Mono<Seller> updateAutoAcceptOrders(SellerId sellerId, boolean autoAcceptOrders);
    Mono<Seller> updateAutoConfirmDelivery(SellerId sellerId, boolean autoConfirmDelivery);
    Mono<Seller> updateBusinessHours(SellerId sellerId, BusinessHours businessHours);
    Mono<Seller> updateOrderStats(SellerId sellerId, Double orderAmount, boolean isCancelled, boolean isRefunded);
    Mono<Seller> updateProductStats(SellerId sellerId, Integer totalProducts, Integer totalActiveProducts, Integer totalOutOfStockProducts);
    Mono<Seller> updateRating(SellerId sellerId, Integer rating);
    Mono<Seller> updateFollowersCount(SellerId sellerId, Integer increment);
    Mono<Seller> updateResponseTime(SellerId sellerId, Double hours);
    Mono<Seller> updateLastActive(SellerId sellerId);
    Mono<Seller> updateLastLogin(SellerId sellerId, LocalDateTime lastLoginAt);
}
