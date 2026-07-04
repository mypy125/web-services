package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.response.UserAuthInfoResponse;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.statistic.SellerStatistics;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.domain.repository.SellerStatisticsRepositoryPort;
import com.mygitgor.seller_service.infrastructure.mapper.SellerMapper;
import com.mygitgor.seller_service.shared.exception.SellerNotFoundException;
import com.mygitgor.seller_service.domain.model.status.AccountStatus;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.domain.model.status.SellerVerificationStatus;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.page.Page;
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
    private final SellerStatisticsRepositoryPort statisticsRepository;
    private final SellerRepositoryPort sellerRepository;
    private final SellerMapper sellerMapper;

    @Override
    public Mono<Seller> getSellerById(SellerId sellerId) {
        log.debug("Fetching seller by ID: {}", sellerId);
        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())));
    }

    @Override
    public Mono<Seller> getSellerByEmail(Email email) {
        log.debug("Fetching seller by email: {}", email);
        return sellerRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(email.toString())));
    }

    @Override
    public Mono<Seller> getSellerByStoreName(String storeName) {
        log.debug("Fetching seller by store name: {}", storeName);
        return sellerRepository.findByStoreName(storeName)
                .switchIfEmpty(Mono.error(new SellerNotFoundException("Store not found: " + storeName)));
    }

    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        log.debug("Checking existence by email: {}", email);
        return sellerRepository.existsByEmail(email);
    }

    @Override
    public Mono<Boolean> existsById(SellerId sellerId) {
        log.debug("Checking existence by ID: {}", sellerId);
        return sellerRepository.existsById(sellerId);
    }

    @Override
    public Mono<Page<Seller>> getAllSellers(int page, int size) {
        log.debug("Fetching page of all sellers: page={}, size={}", page, size);

        return Mono.zip(
                sellerRepository.findAll(page, size).collectList(),
                sellerRepository.countAll()
        ).map(tuple -> Page.of(tuple.getT1(), page, size, tuple.getT2()));
    }

    @Override
    public Mono<Page<Seller>> getSellersByStatus(String status, int page, int size) {
        log.debug("Fetching domain sellers by status: {}, page={}, size={}", status, page, size);
        AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());

        return Mono.zip(
                sellerRepository.findAllByAccountStatus(accountStatus, page, size).collectList(),
                sellerRepository.countByAccountStatus(status)
        ).map(tuple -> Page.of(tuple.getT1(), page, size, tuple.getT2()));
    }

    @Override
    public Mono<Page<Seller>> getSellersByVerificationStatus(String verificationStatus, int page, int size) {
        log.debug("Fetching domain sellers by verification status: {}, page={}, size={}", verificationStatus, page, size);
        SellerVerificationStatus vStatus = SellerVerificationStatus.valueOf(verificationStatus.toUpperCase());

        return Mono.zip(
                sellerRepository.findAllByVerificationStatus(vStatus, page, size).collectList(),
                sellerRepository.countByVerificationStatus(vStatus)
        ).map(tuple -> Page.<Seller>of(tuple.getT1(), page, size, tuple.getT2()));
    }

    @Override
    public Mono<Page<Seller>> getActiveSellers(int page, int size) {
        return getSellersByStatus(AccountStatus.ACTIVE.name(), page, size);
    }

    @Override
    public Mono<Page<Seller>> getVerifiedSellers(int page, int size) {
        return getSellersByVerificationStatus(SellerVerificationStatus.FULLY_VERIFIED.name(), page, size);
    }

    @Override
    public Mono<Page<Seller>> searchSellers(String searchTerm, int page, int size) {
        log.debug("Searching domain sellers by term: {}, page={}, size={}", searchTerm, page, size);

        return Mono.zip(
                sellerRepository.search(searchTerm, page, size).collectList(),
                sellerRepository.countSearchMatches(searchTerm)
        ).map(tuple -> Page.of(tuple.getT1(), page, size, tuple.getT2()));
    }

    @Override
    public Mono<Page<Seller>> getSellersByCategory(String category, int page, int size) {
        log.debug("Fetching domain sellers by category: {}, page={}, size={}", category, page, size);

        return Mono.zip(
                sellerRepository.findAllByCategory(category, page, size).collectList(),
                sellerRepository.countByCategory(category)
        ).map(tuple -> Page.of(tuple.getT1(), page, size, tuple.getT2()));
    }

    @Override
    public Mono<Page<Seller>> getSellersByRating(Double minRating, int page, int size) {
        log.debug("Fetching domain sellers by min rating: {}, page={}, size={}", minRating, page, size);

        return Mono.zip(
                sellerRepository.findAllByMinRating(minRating, page, size).collectList(),
                sellerRepository.countByMinRating(minRating)
        ).map(tuple -> Page.of(tuple.getT1(), page, size, tuple.getT2()));
    }

    @Override
    public Flux<Seller> getTopRatedSellers(int limit) {
        log.debug("Fetching top rated sellers, limit: {}", limit);
        return sellerRepository.findTopRated(limit);
    }

    @Override
    public Mono<Long> countByStatus(String status) {
        return sellerRepository.countByAccountStatus(AccountStatus.valueOf(status.toUpperCase()).name());
    }

    @Override
    public Mono<Long> countByVerificationStatus(String verificationStatus) {
        return sellerRepository.countByVerificationStatus(SellerVerificationStatus.valueOf(verificationStatus.toUpperCase()));
    }

    @Override
    public Mono<Long> countAll() {
        return sellerRepository.countAll();
    }

    @Override
    public Mono<SellerStatistics> getSellerStatistics() {
        log.debug("Aggregating global seller statistics");
        return statisticsRepository.getGlobalStatistics();
    }

    @Override
    public Flux<Seller> getSellersByIds(List<SellerId> sellerIds) {
        log.debug("Fetching multiple sellers by IDs. Count: {}", sellerIds.size());
        return sellerRepository.findAllByIds(sellerIds);
    }

    @Override
    public Flux<Seller> getSellersByEmails(List<Email> emails) {
        log.debug("Fetching multiple sellers by emails. Count: {}", emails.size());
        return sellerRepository.findAllByEmails(emails);
    }

    @Override
    public Mono<Boolean> canSellerSell(SellerId sellerId) {
        return getSellerById(sellerId).map(Seller::canSell);
    }

    @Override
    public Mono<Boolean> canSellerAddProducts(SellerId sellerId) {
        return getSellerById(sellerId).map(Seller::canAddProducts);
    }

    @Override
    public Mono<Boolean> canSellerReceivePayouts(SellerId sellerId) {
        return getSellerById(sellerId).map(Seller::canReceivePayouts);
    }

    @Override
    public Mono<UserAuthInfoResponse> getSellerAuthInfoByEmail(Email email) {
        log.info("Fetching flat auth info mapped model for email: {}", email);
        return sellerRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(email.toString())))
                .map(sellerMapper::toUserAuthInfoResponse);
    }
}
