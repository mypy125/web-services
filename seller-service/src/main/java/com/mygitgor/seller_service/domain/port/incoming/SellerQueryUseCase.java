package com.mygitgor.seller_service.domain.port.incoming;

import com.mygitgor.seller_service.application.dto.response.UserAuthInfoResponse;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.statistic.SellerStatistics;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.page.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SellerQueryUseCase {
    Mono<Seller> getSellerById(SellerId sellerId);
    Mono<Seller> getSellerByEmail(Email email);
    Mono<Seller> getSellerByStoreName(String storeName);
    Mono<Boolean> existsByEmail(Email email);
    Mono<Boolean> existsById(SellerId sellerId);
    Mono<Page<Seller>> getAllSellers(int page, int size);
    Mono<Page<Seller>> getSellersByStatus(String status, int page, int size);
    Mono<Page<Seller>> getSellersByVerificationStatus(String verificationStatus, int page, int size);
    Mono<Page<Seller>> getActiveSellers(int page, int size);
    Mono<Page<Seller>> getVerifiedSellers(int page, int size);
    Mono<Page<Seller>> searchSellers(String searchTerm, int page, int size);
    Mono<Page<Seller>> getSellersByCategory(String category, int page, int size);
    Mono<Page<Seller>> getSellersByRating(Double minRating, int page, int size);
    Flux<Seller> getTopRatedSellers(int limit);
    Mono<Long> countByStatus(String status);
    Mono<Long> countByVerificationStatus(String verificationStatus);
    Mono<Long> countAll();
    Mono<SellerStatistics> getSellerStatistics();
    Flux<Seller> getSellersByIds(List<SellerId> sellerIds);
    Flux<Seller> getSellersByEmails(List<Email> emails);
    Mono<Boolean> canSellerSell(SellerId sellerId);
    Mono<Boolean> canSellerAddProducts(SellerId sellerId);
    Mono<Boolean> canSellerReceivePayouts(SellerId sellerId);
    Mono<UserAuthInfoResponse> getSellerAuthInfoByEmail(Email email);
}
