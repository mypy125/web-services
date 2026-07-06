package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.response.UserAuthInfoResponse;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.statistic.SellerStatistics;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.infrastructure.cache.SellerCacheService;
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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerQueryService implements SellerQueryUseCase {
    private final SellerRepositoryPort sellerRepository;
    private final SellerCacheService cacheService;
    private final SellerMapper sellerMapper;

    @Override
    public Mono<Seller> getSellerById(SellerId sellerId) {
        log.debug("Fetching seller by ID: {}", sellerId);
        return cacheService.getCachedSellerById(sellerId)
                .switchIfEmpty(Mono.defer(() -> sellerRepository.findById(sellerId)
                        .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                        .flatMap(seller -> cacheService.cacheSellerById(seller).thenReturn(seller))));
    }

    @Override
    public Mono<Seller> getSellerByEmail(Email email) {
        log.debug("Fetching seller by email: {}", email);
        return sellerRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(email.toString())))
                .flatMap(seller -> cacheService.cacheSellerById(seller).thenReturn(seller));
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
        return cacheService.getCachedGlobalStatistics()
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Global statistics cache miss. Re-compiling aggregates from repository.");
                    return compileStatisticsFromRepository()
                            .flatMap(freshStats -> {
                                if (freshStats.isEmpty()) {
                                    return Mono.just(freshStats);
                                }
                                return cacheService.cacheGlobalStatistics(freshStats)
                                        .thenReturn(freshStats);
                            });
                }))
                .onErrorResume(e -> {
                    log.error("Fallback standard isolation activated for global statistics aggregation", e);
                    return Mono.just(SellerStatistics.empty());
                });
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

    private Mono<SellerStatistics> compileStatisticsFromRepository() {
        return Mono.zip(
                List.of(
                        sellerRepository.countAll(),
                        sellerRepository.countByAccountStatus(AccountStatus.ACTIVE.name()),
                        sellerRepository.countByAccountStatus(AccountStatus.SUSPENDED.name()),
                        sellerRepository.countByAccountStatus(AccountStatus.BANNED.name()),
                        sellerRepository.countByVerificationStatus(SellerVerificationStatus.PENDING),
                        sellerRepository.countByVerificationStatus(SellerVerificationStatus.FULLY_VERIFIED),
                        sellerRepository.countByVerificationStatus(SellerVerificationStatus.REJECTED),
                        sellerRepository.getAverageRating(),
                        sellerRepository.getAverageOrderValue(),
                        sellerRepository.getAverageCommissionRate(),
                        sellerRepository.getAverageResponseRate(),
                        sellerRepository.getAverageResponseTimeHours(),
                        sellerRepository.getTotalEarnings(),
                        sellerRepository.getTotalSales(),
                        sellerRepository.getTotalCommissionPaid(),
                        sellerRepository.getTotalOrdersCount(),
                        sellerRepository.getTotalProductsCount()
                ),
                args -> SellerStatistics.builder()
                        .totalSellers((Long) args[0])
                        .activeSellers((Long) args[1])
                        .suspendedSellers((Long) args[2])
                        .bannedSellers((Long) args[3])
                        .pendingVerification((Long) args[4])
                        .fullyVerified((Long) args[5])
                        .rejected((Long) args[6])
                        .averageRating((Double) args[7])
                        .averageOrderValue((Double) args[8])
                        .averageCommissionRate((Double) args[9])
                        .averageResponseRate((Double) args[10])
                        .averageResponseTimeHours((Double) args[11])
                        .totalEarnings((Double) args[12])
                        .totalSales((Double) args[13])
                        .totalCommissionPaid((Double) args[14])
                        .totalOrders((Integer) args[15])
                        .totalProducts((Integer) args[16])
                        .calculatedAt(LocalDateTime.now())
                        .build()
        );
    }
}
