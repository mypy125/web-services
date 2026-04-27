package com.mygitgor.auth_service.domain.seller.port;

import com.mygitgor.auth_service.domain.seller.model.*;
import com.mygitgor.auth_service.domain.seller.model.valueobject.Address;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BankDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BusinessDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.SellerId;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface SellerPort {
    Mono<Boolean> existsByEmail(Email email);
    Mono<Seller> getSellerByEmail(Email email);
    Mono<Seller> getSellerById(SellerId sellerId);
    Mono<Seller> getSellerByUserId(UserId userId);
    Mono<Seller> createSeller(Seller seller);
    Mono<Seller> updateSeller(Seller seller);
    Mono<Seller> verifySellerEmail(Email email);
    Mono<Seller> verifyBusinessDocuments(SellerId sellerId, String verifiedBy);
    Mono<Seller> updateAccountStatus(SellerId sellerId, String status, String reason);
    Mono<Seller> updatePayoutSettings(SellerId sellerId, BankDetails bankDetails);
    Mono<Seller> updateBusinessDetails(SellerId sellerId, BusinessDetails businessDetails);
    Mono<Seller> updatePickupAddress(SellerId sellerId, Address pickupAddress);
    Mono<SellerBalance> getSellerBalance(SellerId sellerId);
    Flux<PayoutTransaction> getPayoutHistory(SellerId sellerId, int limit, int offset);
    Mono<SellerStatistics> getSellerStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<Long> getProductCount(SellerId sellerId);
    Mono<Double> getTotalSales(SellerId sellerId);
    Mono<SellerRating> getSellerRating(SellerId sellerId);
    Mono<Boolean> canAddProducts(SellerId sellerId);
    Mono<Boolean> isSellerVerified(Email email);
    Mono<Page<Seller>> getAllSellers(String status, int page, int size);
    Mono<Page<Seller>> searchSellers(String searchTerm, int page, int size);
    Mono<Page<Seller>> getPendingVerifications(int page, int size);
    Mono<Void> updateLastActive(SellerId sellerId, LocalDateTime lastActiveAt);
    Mono<Seller> updateCommissionRate(SellerId sellerId, double commissionRate);
}
