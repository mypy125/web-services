package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.statistic.SellerStatistics;
import com.mygitgor.seller_service.domain.model.shared.valueobject.Email;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.model.shared.valueobject.page.Page;
import com.mygitgor.seller_service.domain.port.incoming.SellerQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerQueryService implements SellerQueryUseCase {
    @Override
    public Mono<Seller> getSellerById(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Seller> getSellerByEmail(Email email) {
        return null;
    }

    @Override
    public Mono<Seller> getSellerByStoreName(String storeName) {
        return null;
    }

    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        return null;
    }

    @Override
    public Mono<Boolean> existsById(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> getAllSellers(int page, int size) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> getSellersByStatus(String status, int page, int size) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> getSellersByVerificationStatus(String verificationStatus, int page, int size) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> getActiveSellers(int page, int size) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> getVerifiedSellers(int page, int size) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> searchSellers(String searchTerm, int page, int size) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> getSellersByCategory(String category, int page, int size) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> getSellersByRating(Double minRating, int page, int size) {
        return null;
    }

    @Override
    public Flux<Seller> getTopRatedSellers(int limit) {
        return null;
    }

    @Override
    public Mono<Long> countByStatus(String status) {
        return null;
    }

    @Override
    public Mono<Long> countByVerificationStatus(String verificationStatus) {
        return null;
    }

    @Override
    public Mono<Long> countAll() {
        return null;
    }

    @Override
    public Mono<SellerStatistics> getSellerStatistics() {
        return null;
    }

    @Override
    public Flux<Seller> getSellersByIds(List<SellerId> sellerIds) {
        return null;
    }

    @Override
    public Flux<Seller> getSellersByEmails(List<Email> emails) {
        return null;
    }

    @Override
    public Mono<Boolean> canSellerSell(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Boolean> canSellerAddProducts(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Boolean> canSellerReceivePayouts(SellerId sellerId) {
        return null;
    }
}
