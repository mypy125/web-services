package com.mygitgor.transaction_service.infrastructure.persistence;

import com.mygitgor.transaction_service.domain.model.Settlement;
import com.mygitgor.transaction_service.domain.model.SettlementStatistics;
import com.mygitgor.transaction_service.domain.model.valueobject.SettlementStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.SettlementType;
import com.mygitgor.transaction_service.domain.port.outgoing.SettlementRepositoryPort;
import com.mygitgor.transaction_service.infrastructure.persistence.entity.SettlementEntity;
import com.mygitgor.transaction_service.infrastructure.persistence.mapper.SettlementPersistenceMapper;
import com.mygitgor.transaction_service.infrastructure.persistence.repository.SettlementR2dbcRepository;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.SettlementId;
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
public class SettlementRepositoryAdapter implements SettlementRepositoryPort {
    private final SettlementR2dbcRepository repository;
    private final SettlementPersistenceMapper mapper;

    @Override
    public Mono<Settlement> save(Settlement settlement) {
        log.debug("Saving settlement: {}", settlement.getSettlementId());
        SettlementEntity entity = mapper.toEntity(settlement);
        return repository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(s -> log.debug("Settlement saved: {}", s.getSettlementId()))
                .doOnError(e -> log.error("Failed to save settlement: {}", e.getMessage()));
    }

    @Override
    public Mono<Settlement> updateStatus(SettlementId settlementId, SettlementStatus status) {
        return repository.findById(UUID.fromString(settlementId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Settlement not found: " + settlementId)))
                .flatMap(entity -> {
                    SettlementEntity updated = entity
                            .withStatus(status.name())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Settlement> processSettlement(SettlementId settlementId, String processedBy) {
        return repository.findById(UUID.fromString(settlementId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Settlement not found: " + settlementId)))
                .flatMap(entity -> {
                    SettlementEntity updated = entity
                            .withStatus(SettlementStatus.PROCESSED.name())
                            .withProcessedAt(LocalDateTime.now())
                            .withProcessedBy(processedBy)
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Settlement> completeSettlement(SettlementId settlementId, String gatewayTransactionId) {
        return repository.findById(UUID.fromString(settlementId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Settlement not found: " + settlementId)))
                .flatMap(entity -> {
                    SettlementEntity updated = entity
                            .withStatus(SettlementStatus.COMPLETED.name())
                            .withCompletedAt(LocalDateTime.now())
                            .withGatewayTransactionId(gatewayTransactionId)
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Settlement> settleSettlement(SettlementId settlementId) {
        return repository.findById(UUID.fromString(settlementId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Settlement not found: " + settlementId)))
                .flatMap(entity -> {
                    SettlementEntity updated = entity
                            .withStatus(SettlementStatus.SETTLED.name())
                            .withSettledAt(LocalDateTime.now())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Settlement> failSettlement(SettlementId settlementId, String reason) {
        return repository.findById(UUID.fromString(settlementId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Settlement not found: " + settlementId)))
                .flatMap(entity -> {
                    SettlementEntity updated = entity
                            .withStatus(SettlementStatus.FAILED.name())
                            .withFailureReason(reason)
                            .withFailedAt(LocalDateTime.now())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(SettlementId settlementId) {
        return repository.deleteById(UUID.fromString(settlementId.toString()));
    }

    @Override
    public Mono<Settlement> findById(SettlementId settlementId) {
        return repository.findById(UUID.fromString(settlementId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Settlement> findByReferenceNumber(String referenceNumber) {
        return repository.findByReferenceNumber(referenceNumber)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Settlement> findBySellerId(SellerId sellerId, int page, int size) {
        return repository.findBySellerId(
                        UUID.fromString(sellerId.toString()),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Settlement> findBySellerIdAndStatus(SellerId sellerId, SettlementStatus status, int page, int size) {
        return repository.findBySellerIdAndStatus(
                        UUID.fromString(sellerId.toString()),
                        status.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Settlement> findBySellerIdAndType(SellerId sellerId, SettlementType type, int page, int size) {
        return repository.findBySellerIdAndType(
                        UUID.fromString(sellerId.toString()),
                        type.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Settlement> findBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {
        return repository.findBySellerIdAndCreatedAtBetween(
                        UUID.fromString(sellerId.toString()),
                        startDate,
                        endDate,
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Settlement> findByStatus(SettlementStatus status, int page, int size) {
        return repository.findByStatus(status.name(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Settlement> findByIds(List<SettlementId> settlementIds) {
        List<UUID> uuids = settlementIds.stream()
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
    public Mono<Long> countBySellerIdAndStatus(SellerId sellerId, SettlementStatus status) {
        return repository.countBySellerIdAndStatus(
                UUID.fromString(sellerId.toString()), status.name());
    }

    @Override
    public Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.countBySellerIdAndCreatedAtBetween(
                UUID.fromString(sellerId.toString()), startDate, endDate);
    }

    @Override
    public Mono<Long> countPendingSettlements() {
        return repository.countByStatus(SettlementStatus.PENDING.name());
    }

    @Override
    public Mono<Double> sumAmountBySellerId(SellerId sellerId) {
        return repository.sumAmountBySellerId(UUID.fromString(sellerId.toString()))
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Double> sumAmountBySellerIdAndStatus(SellerId sellerId, SettlementStatus status) {
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
    public Mono<Double> sumNetAmountBySellerId(SellerId sellerId) {
        return repository.sumNetAmountBySellerId(UUID.fromString(sellerId.toString()))
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<SettlementStatistics> getSettlementStatistics(SellerId sellerId) {
        UUID sellerUuid = UUID.fromString(sellerId.toString());

        return Mono.zip(
                repository.countBySellerId(sellerUuid),
                repository.countBySellerIdAndStatus(sellerUuid, SettlementStatus.COMPLETED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, SettlementStatus.SETTLED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, SettlementStatus.PENDING.name()),
                repository.countBySellerIdAndStatus(sellerUuid, SettlementStatus.FAILED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, SettlementStatus.CANCELLED.name()),
                repository.sumAmountBySellerId(sellerUuid).defaultIfEmpty(0.0),
                repository.sumNetAmountBySellerId(sellerUuid).defaultIfEmpty(0.0)
        ).map(tuple -> {
            Long total = tuple.getT1();
            BigDecimal totalAmount = toBigDecimal(tuple.getT7());
            BigDecimal totalNetAmount = toBigDecimal(tuple.getT8());

            BigDecimal avgAmount = total > 0
                    ? totalAmount.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return SettlementStatistics.builder()
                    .totalSettlements(total)
                    .completedSettlements(tuple.getT2())
                    .settledSettlements(tuple.getT3())
                    .pendingSettlements(tuple.getT4())
                    .failedSettlements(tuple.getT5())
                    .cancelledSettlements(tuple.getT6())
                    .totalAmount(totalAmount)
                    .totalNetAmount(totalNetAmount)
                    .averageSettlementAmount(avgAmount)
                    .calculatedAt(LocalDateTime.now())
                    .build();
        });
    }

    @Override
    public Mono<SettlementStatistics> getSettlementStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        UUID sellerUuid = UUID.fromString(sellerId.toString());

        return Mono.zip(
                repository.countBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate),
                repository.sumAmountBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0),
                repository.sumNetAmountBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0)
        ).map(tuple -> {
            Long total = tuple.getT1();
            BigDecimal totalAmount = toBigDecimal(tuple.getT2());
            BigDecimal totalNetAmount = toBigDecimal(tuple.getT3());

            BigDecimal avgAmount = total > 0
                    ? totalAmount.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return SettlementStatistics.builder()
                    .totalSettlements(total)
                    .totalAmount(totalAmount)
                    .totalNetAmount(totalNetAmount)
                    .averageSettlementAmount(avgAmount)
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
