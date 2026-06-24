package com.mygitgor.seller_service.domain.repository;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.valueobject.Email;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SellerRepositoryPort {
    Mono<Seller> save(Seller seller);
    Mono<Void> deleteById(SellerId sellerId);
    Mono<Void> deleteByEmail(Email email);
    Mono<Seller> findById(SellerId sellerId);
    Mono<Seller> findByEmail(Email email);
    Mono<Seller> findByStoreName(String storeName);
    Flux<Seller> findAll(int page, int size);
    Flux<Seller> findByAccountStatus(String status, int page, int size);
    Flux<Seller> findByVerificationStatus(String verificationStatus, int page, int size);
    Flux<Seller> findActiveSellers(int page, int size);
    Flux<Seller> findVerifiedSellers(int page, int size);
    Flux<Seller> findByStoreCategory(String category, int page, int size);
    Flux<Seller> findByAverageRatingGreaterThan(Double minRating, int page, int size);
    Flux<Seller> findTopRatedSellers(int limit);
    Flux<Seller> search(String searchTerm, int page, int size);
    Flux<Seller> findByIds(List<SellerId> sellerIds);
    Flux<Seller> findByEmails(List<Email> emails);
    Mono<Boolean> existsByEmail(Email email);
    Mono<Boolean> existsById(SellerId sellerId);
    Mono<Long> count();
    Mono<Long> countByAccountStatus(String status);
    Mono<Long> countByVerificationStatus(String verificationStatus);
    Mono<Long> countActiveSellers();
    Mono<Long> countVerifiedSellers();
    Mono<Seller> updateAccountStatus(SellerId sellerId, String status, String reason);
    Mono<Seller> updateVerificationStatus(SellerId sellerId, String verificationStatus, String reason);
    Mono<Seller> updateRating(SellerId sellerId, Double rating, Integer totalReviews);
    Mono<Seller> updateStatistics(SellerId sellerId, Seller statistics);
    Mono<Seller> updateCommissionRate(SellerId sellerId, Double commissionRate);
    Mono<Seller> updateLastActive(SellerId sellerId);
}
