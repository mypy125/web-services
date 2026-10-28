package com.mygitgor.transaction_service.infrastructure.persistence;

import com.mygitgor.transaction_service.domain.model.Payout;
import com.mygitgor.transaction_service.domain.model.PayoutStatistics;
import com.mygitgor.transaction_service.domain.model.valueobject.PayoutStatus;
import com.mygitgor.transaction_service.domain.port.outgoing.PayoutRepositoryPort;
import com.mygitgor.transaction_service.infrastructure.persistence.entity.PayoutEntity;
import com.mygitgor.transaction_service.infrastructure.persistence.mapper.PayoutPersistenceMapper;
import com.mygitgor.transaction_service.infrastructure.persistence.repository.PayoutR2dbcRepository;
import com.mygitgor.transaction_service.shared.valueobject.PayoutId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutRepositoryAdapter implements PayoutRepositoryPort {
    private final PayoutR2dbcRepository repository;
    private final PayoutPersistenceMapper mapper;

    @Override
    public Mono<Payout> save(Payout payout) {
        log.debug("Saving payout: {}", payout.getPayoutId());
        PayoutEntity entity = mapper.toEntity(payout);
        return repository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(p -> log.debug("Payout saved: {}", p.getPayoutId()))
                .doOnError(e -> log.error("Failed to save payout: {}", e.getMessage()));
    }

    @Override
    public Mono<Payout> updateStatus(PayoutId payoutId, PayoutStatus status) {
        return repository.findById(UUID.fromString(payoutId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Payout not found: " + payoutId)))
                .flatMap(entity -> {
                    PayoutEntity updated = entity
                            .withStatus(status.name())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Payout> completePayout(PayoutId payoutId, String gatewayTransactionId) {
        return repository.findById(UUID.fromString(payoutId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Payout not found: " + payoutId)))
                .flatMap(entity -> {
                    PayoutEntity updated = entity
                            .withStatus(PayoutStatus.COMPLETED.name())
                            .withCompletedAt(LocalDateTime.now())
                            .withGatewayTransactionId(gatewayTransactionId)
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Payout> failPayout(PayoutId payoutId, String reason) {
        return repository.findById(UUID.fromString(payoutId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Payout not found: " + payoutId)))
                .flatMap(entity -> {
                    PayoutEntity updated = entity
                            .withStatus(PayoutStatus.FAILED.name())
                            .withFailureReason(reason)
                            .withFailedAt(LocalDateTime.now())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(PayoutId payoutId) {
        return repository.deleteById(UUID.fromString(payoutId.toString()));
    }

    @Override
    public Mono<Payout> findById(PayoutId payoutId) {
        return repository.findById(UUID.fromString(payoutId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Payout> findByReferenceNumber(String referenceNumber) {
        return repository.findByReferenceNumber(referenceNumber)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Payout> findBySellerId(SellerId sellerId, int page, int size) {
        return repository.findBySellerId(
                        UUID.fromString(sellerId.toString()),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Payout> findBySellerIdAndStatus(SellerId sellerId, PayoutStatus status, int page, int size) {
        return repository.findBySellerIdAndStatus(
                        UUID.fromString(sellerId.toString()),
                        status.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Payout> findBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        return repository.findBySellerIdAndCreatedAtBetween(
                        UUID.fromString(sellerId.toString()),
                        startDate,
                        endDate,
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Payout> findByStatus(PayoutStatus status, int page, int size) {
        return repository.findByStatus(status.name(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Payout> findByIds(List<PayoutId> payoutIds) {
        List<UUID> uuids = payoutIds.stream()
                .map(id -> UUID.fromString(id.toString()))
                .toList();
        return repository.findByIdIn(uuids)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countBySellerId(SellerId sellerId) {
        return repository.countBySellerId(UUID.fromString(sellerId.toString()));
    }

    @Override
    public Mono<Long> countBySellerIdAndStatus(SellerId sellerId, PayoutStatus status) {
        return repository.countBySellerIdAndStatus(
                UUID.fromString(sellerId.toString()), status.name());
    }

    @Override
    public Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.countBySellerIdAndCreatedAtBetween(
                UUID.fromString(sellerId.toString()), startDate, endDate);
    }

    @Override
    public Mono<Long> countPendingPayouts() {
        return repository.countByStatus(PayoutStatus.PENDING.name());
    }

    @Override
    public Mono<Double> sumAmountBySellerId(SellerId sellerId) {
        return repository.sumAmountBySellerId(UUID.fromString(sellerId.toString()))
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> sumAmountBySellerIdAndStatus(SellerId sellerId, PayoutStatus status) {
        return repository.sumAmountBySellerIdAndStatus(
                        UUID.fromString(sellerId.toString()), status.name())
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> sumAmountBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return repository.sumAmountBySellerIdAndCreatedAtBetween(
                        UUID.fromString(sellerId.toString()), startDate, endDate)
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<PayoutStatistics> getPayoutStatistics(SellerId sellerId) {
        UUID sellerUuid = UUID.fromString(sellerId.toString());

        return Mono.zip(
                repository.countBySellerId(sellerUuid),
                repository.countBySellerIdAndStatus(sellerUuid, PayoutStatus.COMPLETED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, PayoutStatus.PENDING.name()),
                repository.countBySellerIdAndStatus(sellerUuid, PayoutStatus.FAILED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, PayoutStatus.CANCELLED.name()),
                repository.sumAmountBySellerId(sellerUuid).defaultIfEmpty(0.0),
                repository.sumFeeBySellerId(sellerUuid).defaultIfEmpty(0.0),
                repository.sumNetAmountBySellerId(sellerUuid).defaultIfEmpty(0.0)
        ).map(tuple -> {
            Long total = tuple.getT1();
            BigDecimal totalAmount = toBigDecimal(tuple.getT6());
            BigDecimal totalFees = toBigDecimal(tuple.getT7());
            BigDecimal totalNetAmount = toBigDecimal(tuple.getT8());

            BigDecimal avgAmount = total > 0
                    ? totalAmount.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return PayoutStatistics.builder()
                    .totalPayouts(total)
                    .completedPayouts(tuple.getT2())
                    .pendingPayouts(tuple.getT3())
                    .failedPayouts(tuple.getT4())
                    .cancelledPayouts(tuple.getT5())
                    .totalAmount(totalAmount)
                    .totalFees(totalFees)
                    .totalNetAmount(totalNetAmount)
                    .averagePayoutAmount(avgAmount)
                    .calculatedAt(LocalDateTime.now())
                    .build();
        });
    }

    @Override
    public Mono<PayoutStatistics> getPayoutStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        UUID sellerUuid = UUID.fromString(sellerId.toString());

        return Mono.zip(
                repository.countBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate),
                repository.sumAmountBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0),
                repository.sumFeeBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0),
                repository.sumNetAmountBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0)
        ).map(tuple -> {
            Long total = tuple.getT1();
            BigDecimal totalAmount = toBigDecimal(tuple.getT2());
            BigDecimal totalFees = toBigDecimal(tuple.getT3());
            BigDecimal totalNetAmount = toBigDecimal(tuple.getT4());

            BigDecimal avgAmount = total > 0
                    ? totalAmount.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return PayoutStatistics.builder()
                    .totalPayouts(total)
                    .totalAmount(totalAmount)
                    .totalFees(totalFees)
                    .totalNetAmount(totalNetAmount)
                    .averagePayoutAmount(avgAmount)
                    .calculatedAt(LocalDateTime.now())
                    .build();
        });
    }

    private BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case null -> BigDecimal.ZERO;
            case BigDecimal bigDecimal -> bigDecimal;
            case Double v -> BigDecimal.valueOf(v);
            case Long l -> BigDecimal.valueOf(l);
            default -> new BigDecimal(value.toString());
        };
    }
}
