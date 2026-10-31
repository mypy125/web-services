package com.mygitgor.seller_service.infrastructure.persistence;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.status.AccountStatus;
import com.mygitgor.seller_service.domain.model.status.SellerVerificationStatus;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerEntity;
import com.mygitgor.seller_service.infrastructure.persistence.mapper.SellerPersistenceMapper;
import com.mygitgor.seller_service.infrastructure.persistence.repository.SellerR2dbcRepository;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SellerRepositoryAdapter implements SellerRepositoryPort {
    private final SellerR2dbcRepository repository;
    private final SellerPersistenceMapper mapper;

    @Override
    public Mono<Seller> save(Seller seller) {
        log.debug("Saving seller: {}", seller.getSellerId());
        SellerEntity entity = mapper.toEntity(seller);
        return repository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(s -> log.debug("Seller saved: {}", s.getSellerId()))
                .doOnError(e -> log.error("Failed to save seller: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> deleteById(SellerId sellerId) {
        log.debug("Deleting seller by ID: {}", sellerId);
        return repository.deleteById(UUID.fromString(sellerId.toString()))
                .doOnSuccess(v -> log.debug("Seller deleted: {}", sellerId));
    }

    @Override
    public Mono<Void> deleteByEmail(Email email) {
        log.debug("Deleting seller by email: {}", email);
        return repository.deleteByEmail(email.toString())
                .doOnSuccess(v -> log.debug("Seller deleted by email: {}", email));
    }

    @Override
    public Mono<Seller> findById(SellerId sellerId) {
        log.debug("Finding seller by ID: {}", sellerId);
        return repository.findById(UUID.fromString(sellerId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Seller> findByEmail(Email email) {
        log.debug("Finding seller by email: {}", email);
        return repository.findByEmail(email.toString())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Seller> findByStoreName(String storeName) {
        log.debug("Finding seller by store name: {}", storeName);
        return repository.findByStoreName(storeName)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findAll(int page, int size) {
        log.debug("Finding all sellers - page: {}, size: {}", page, size);
        return repository.findAllBy(PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findByAccountStatus(String status, int page, int size) {
        log.debug("Finding sellers by account status: {}", status);
        return repository.findByAccountStatus(status, PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findByVerificationStatus(String verificationStatus, int page, int size) {
        log.debug("Finding sellers by verification status: {}", verificationStatus);
        return repository.findByVerificationStatus(verificationStatus, PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findAllByAccountStatus(AccountStatus accountStatus, int page, int size) {
        log.debug("Finding sellers by AccountStatus: {}", accountStatus);
        return repository.findByAccountStatus(accountStatus.name(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findAllByVerificationStatus(SellerVerificationStatus verificationStatus, int page, int size) {
        log.debug("Finding sellers by SellerVerificationStatus: {}", verificationStatus);
        return repository.findByVerificationStatus(verificationStatus.name(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findActiveSellers(int page, int size) {
        log.debug("Finding active sellers - page: {}, size: {}", page, size);
        return repository.findByAccountStatus(AccountStatus.ACTIVE.name(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findVerifiedSellers(int page, int size) {
        log.debug("Finding verified sellers - page: {}, size: {}", page, size);
        return repository.findByVerificationStatus(
                        SellerVerificationStatus.FULLY_VERIFIED.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findByStoreCategory(String category, int page, int size) {
        log.debug("Finding sellers by store category: {}", category);
        return repository.findByStoreCategory(category, PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findByAverageRatingGreaterThan(Double minRating, int page, int size) {
        log.debug("Finding sellers with rating greater than: {}", minRating);
        return repository.findByAverageRatingGreaterThan(minRating, PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findTopRatedSellers(int limit) {
        log.debug("Finding top rated sellers, limit: {}", limit);
        return repository.findTopRated(PageRequest.of(0, limit))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> search(String searchTerm, int page, int size) {
        log.debug("Searching sellers with term: {}", searchTerm);
        return repository.search(searchTerm, PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findByIds(List<SellerId> sellerIds) {
        log.debug("Finding sellers by IDs: {}", sellerIds.size());
        List<UUID> uuids = sellerIds.stream()
                .map(id -> UUID.fromString(id.toString()))
                .toList();
        return repository.findByIdIn(uuids)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Seller> findByEmails(List<Email> emails) {
        log.debug("Finding sellers by emails: {}", emails.size());
        List<String> emailStrings = emails.stream()
                .map(Email::toString)
                .toList();
        return repository.findByEmailIn(emailStrings)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        log.debug("Checking existence by email: {}", email);
        return repository.existsByEmail(email.toString());
    }

    @Override
    public Mono<Boolean> existsById(SellerId sellerId) {
        log.debug("Checking existence by ID: {}", sellerId);
        return repository.existsById(UUID.fromString(sellerId.toString()));
    }

    @Override
    public Mono<Long> count() {
        log.debug("Counting all sellers");
        return repository.count();
    }

    @Override
    public Mono<Long> countByAccountStatus(String status) {
        log.debug("Counting sellers by account status: {}", status);
        return repository.countByAccountStatus(status);
    }

    @Override
    public Mono<Long> countByVerificationStatus(SellerVerificationStatus status) {
        log.debug("Counting sellers by verification status: {}", status);
        return repository.countByVerificationStatus(status.name());
    }

    @Override
    public Mono<Long> countActiveSellers() {
        return repository.countByAccountStatus(AccountStatus.ACTIVE.name());
    }

    @Override
    public Mono<Long> countVerifiedSellers() {
        return repository.countByVerificationStatus(SellerVerificationStatus.FULLY_VERIFIED.name());
    }

    @Override
    public Mono<Seller> updateAccountStatus(SellerId sellerId, String status, String reason) {
        log.info("Updating account status for seller: {} to {}", sellerId, status);
        return repository.findById(UUID.fromString(sellerId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Seller not found: " + sellerId)))
                .flatMap(entity -> {
                    SellerEntity updateAccountStatus = entity
                            .withAccountStatus(status)
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updateAccountStatus);
                })
                .map(mapper::toDomain)
                .doOnSuccess(s -> log.info("Account status updated for seller: {}", sellerId));
    }

    @Override
    public Mono<Seller> updateVerificationStatus(SellerId sellerId, String verificationStatus, String reason) {
        log.info("Updating verification status for seller: {} to {}", sellerId, verificationStatus);
        return repository.findById(UUID.fromString(sellerId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Seller not found: " + sellerId)))
                .flatMap(entity -> {
                    SellerEntity updateVerificationStatus = entity
                            .withVerificationStatus(verificationStatus)
                            .withRejectionReason(reason)
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updateVerificationStatus);
                })
                .map(mapper::toDomain)
                .doOnSuccess(s -> log.info("Verification status updated for seller: {}", sellerId));
    }

    @Override
    public Mono<Seller> updateRating(SellerId sellerId, Double rating, Integer totalReviews) {
        log.info("Updating rating for seller: {} to {} ({} reviews)", sellerId, rating, totalReviews);
        return repository.findById(UUID.fromString(sellerId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Seller not found: " + sellerId)))
                .flatMap(entity -> {
                    SellerEntity updateRating = entity
                            .withAverageRating(rating)
                            .withTotalReviews(totalReviews)
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updateRating);
                })
                .map(mapper::toDomain)
                .doOnSuccess(s -> log.info("Rating updated for seller: {}", sellerId));
    }

    @Override
    public Mono<Seller> updateStatistics(SellerId sellerId, Seller statistics) {
        log.info("Updating statistics for seller: {}", sellerId);
        return repository.findById(UUID.fromString(sellerId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Seller not found: " + sellerId)))
                .flatMap(entity -> {
                    SellerEntity updateStatistics = entity
                            .withTotalOrders(statistics.getTotalOrders())
                            .withTotalProducts(statistics.getTotalProducts())
                            .withTotalActiveProducts(statistics.getTotalActiveProducts())
                            .withTotalOutOfStockProducts(statistics.getTotalOutOfStockProducts())
                            .withTotalEarnings(statistics.getTotalEarnings())
                            .withTotalSales(statistics.getTotalSales())
                            .withTotalRefunds(statistics.getTotalRefunds())
                            .withTotalTax(statistics.getTotalTax())
                            .withNetEarnings(statistics.getNetEarnings())
                            .withAverageOrderValue(statistics.getAverageOrderValue())
                            .withConversionRate(statistics.getConversionRate())
                            .withReturnRate(statistics.getReturnRate())
                            .withCancellationRate(statistics.getCancellationRate())
                            .withCanceledOrders(statistics.getCanceledOrders())
                            .withTotalTransactions(statistics.getTotalTransactions())
                            .withTotalCommissionPaid(statistics.getTotalCommissionPaid())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updateStatistics);
                })
                .map(mapper::toDomain)
                .doOnSuccess(s -> log.info("Statistics updated for seller: {}", sellerId));
    }

    @Override
    public Mono<Seller> updateCommissionRate(SellerId sellerId, Double commissionRate) {
        log.info("Updating commission rate for seller: {} to {}", sellerId, commissionRate);
        return repository.findById(UUID.fromString(sellerId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Seller not found: " + sellerId)))
                .flatMap(entity -> {
                    SellerEntity updateCommissionRate = entity
                            .withCommissionRate(commissionRate)
                            .withCommissionUpdatedAt(LocalDateTime.now())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updateCommissionRate);
                })
                .map(mapper::toDomain)
                .doOnSuccess(s -> log.info("Commission rate updated for seller: {}", sellerId));
    }

    @Override
    public Mono<Seller> updateLastActive(SellerId sellerId) {
        log.debug("Updating last active for seller: {}", sellerId);
        return repository.findById(UUID.fromString(sellerId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Seller not found: " + sellerId)))
                .flatMap(entity -> {
                    SellerEntity updateLastActive = entity
                            .withLastActiveAt(LocalDateTime.now())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updateLastActive);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countAll() {
        return count();
    }

    @Override
    public Mono<Long> countSearchMatches(String searchTerm) {
        log.debug("Counting search matches for: {}", searchTerm);
        return repository.countBySearchTerm(searchTerm);
    }

    @Override
    public Flux<Seller> findAllByCategory(String category, int page, int size) {
        return findByStoreCategory(category, page, size);
    }

    @Override
    public Mono<Long> countByCategory(String category) {
        log.debug("Counting sellers by category: {}", category);
        return repository.countByStoreCategory(category);
    }

    @Override
    public Flux<Seller> findAllByMinRating(Double minRating, int page, int size) {
        return findByAverageRatingGreaterThan(minRating, page, size);
    }

    @Override
    public Mono<Long> countByMinRating(Double minRating) {
        log.debug("Counting sellers by min rating: {}", minRating);
        return repository.countByAverageRatingGreaterThan(minRating);
    }

    @Override
    public Flux<Seller> findTopRated(int limit) {
        return findTopRatedSellers(limit);
    }

    @Override
    public Flux<Seller> findAllByIds(List<SellerId> sellerIds) {
        return findByIds(sellerIds);
    }

    @Override
    public Flux<Seller> findAllByEmails(List<Email> emails) {
        return findByEmails(emails);
    }

    @Override
    public Mono<Double> getAverageRating() {
        log.debug("Getting average rating for all sellers");
        return repository.getAverageRating().defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> getAverageOrderValue() {
        log.debug("Getting average order value for all sellers");
        return repository.getAverageOrderValue().defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> getAverageCommissionRate() {
        log.debug("Getting average commission rate for all sellers");
        return repository.getAverageCommissionRate().defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> getAverageResponseRate() {
        log.debug("Getting average response rate for all sellers");
        return repository.getAverageResponseRate().defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> getAverageResponseTimeHours() {
        log.debug("Getting average response time hours for all sellers");
        return repository.getAverageResponseTimeHours().defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> getTotalEarnings() {
        log.debug("Getting total earnings for all sellers");
        return repository.getTotalEarnings().defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> getTotalSales() {
        log.debug("Getting total sales for all sellers");
        return repository.getTotalSales().defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> getTotalCommissionPaid() {
        log.debug("Getting total commission paid for all sellers");
        return repository.getTotalCommissionPaid().defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Integer> getTotalOrdersCount() {
        log.debug("Getting total orders count for all sellers");
        return repository.getTotalOrdersCount().defaultIfEmpty(0);
    }

    @Override
    public Mono<Integer> getTotalProductsCount() {
        log.debug("Getting total products count for all sellers");
        return repository.getTotalProductsCount().defaultIfEmpty(0);
    }
}
