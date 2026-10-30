package com.mygitgor.transaction_service.infrastructure.persistence;

import com.mygitgor.transaction_service.domain.model.Reconciliation;
import com.mygitgor.transaction_service.domain.model.ReconciliationStatistics;
import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationType;
import com.mygitgor.transaction_service.domain.port.outgoing.ReconciliationRepositoryPort;
import com.mygitgor.transaction_service.infrastructure.persistence.entity.ReconciliationEntity;
import com.mygitgor.transaction_service.infrastructure.persistence.mapper.ReconciliationPersistenceMapper;
import com.mygitgor.transaction_service.infrastructure.persistence.repository.ReconciliationR2dbcRepository;
import com.mygitgor.transaction_service.shared.valueobject.ReconciliationId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationRepositoryAdapter implements ReconciliationRepositoryPort {
    private final ReconciliationR2dbcRepository repository;
    private final ReconciliationPersistenceMapper mapper;

    @Override
    public Mono<Reconciliation> save(Reconciliation reconciliation) {
        log.debug("Saving reconciliation: {}", reconciliation.getReconciliationId());
        ReconciliationEntity entity = mapper.toEntity(reconciliation);
        return repository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(r -> log.debug("Reconciliation saved: {}", r.getReconciliationId()))
                .doOnError(e -> log.error("Failed to save reconciliation: {}", e.getMessage()));
    }

    @Override
    public Mono<Reconciliation> updateStatus(ReconciliationId reconciliationId, ReconciliationStatus status) {
        return repository.findById(UUID.fromString(reconciliationId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Reconciliation not found: " + reconciliationId)))
                .flatMap(entity -> {
                    ReconciliationEntity updated = entity
                            .withStatus(status.name())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Reconciliation> reconcile(ReconciliationId reconciliationId, String resolvedBy) {
        return repository.findById(UUID.fromString(reconciliationId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Reconciliation not found: " + reconciliationId)))
                .flatMap(entity -> {
                    ReconciliationEntity updated = entity
                            .withStatus(ReconciliationStatus.RECONCILED.name())
                            .withResolvedBy(resolvedBy)
                            .withResolvedAt(LocalDateTime.now())
                            .withReconciledAt(LocalDateTime.now())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Reconciliation> reject(ReconciliationId reconciliationId, String reason) {
        return repository.findById(UUID.fromString(reconciliationId.toString()))
                .switchIfEmpty(Mono.error(new RuntimeException("Reconciliation not found: " + reconciliationId)))
                .flatMap(entity -> {
                    ReconciliationEntity updated = entity
                            .withStatus(ReconciliationStatus.REJECTED.name())
                            .withDiscrepancyReason(reason)
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(ReconciliationId reconciliationId) {
        return repository.deleteById(UUID.fromString(reconciliationId.toString()));
    }

    @Override
    public Mono<Reconciliation> findById(ReconciliationId reconciliationId) {
        return repository.findById(UUID.fromString(reconciliationId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Reconciliation> findByPeriod(SellerId sellerId, String period) {
        return repository.findBySellerIdAndPeriod(
                        UUID.fromString(sellerId.toString()), period)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Reconciliation> findBySellerId(SellerId sellerId, int page, int size) {
        return repository.findBySellerId(
                        UUID.fromString(sellerId.toString()),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Reconciliation> findBySellerIdAndStatus(SellerId sellerId, ReconciliationStatus status, int page, int size) {
        return repository.findBySellerIdAndStatus(
                        UUID.fromString(sellerId.toString()),
                        status.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Reconciliation> findBySellerIdAndType(SellerId sellerId, ReconciliationType type, int page, int size) {
        return repository.findBySellerIdAndType(
                        UUID.fromString(sellerId.toString()),
                        type.name(),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Reconciliation> findBySellerIdAndDateBetween(
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
    public Flux<Reconciliation> findByStatus(ReconciliationStatus status, int page, int size) {
        return repository.findByStatus(status.name(), PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Reconciliation> findByIds(List<ReconciliationId> reconciliationIds) {
        List<UUID> uuids = reconciliationIds.stream()
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
    public Mono<Long> countBySellerIdAndStatus(SellerId sellerId, ReconciliationStatus status) {
        return repository.countBySellerIdAndStatus(
                UUID.fromString(sellerId.toString()), status.name());
    }

    @Override
    public Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.countBySellerIdAndCreatedAtBetween(
                UUID.fromString(sellerId.toString()), startDate, endDate);
    }

    @Override
    public Mono<Long> countPendingReconciliations() {
        return repository.countByStatus(ReconciliationStatus.PENDING.name());
    }

    @Override
    public Mono<ReconciliationStatistics> getReconciliationStatistics(SellerId sellerId) {
        UUID sellerUuid = UUID.fromString(sellerId.toString());

        return Mono.zip(
                repository.countBySellerId(sellerUuid),
                repository.countBySellerIdAndStatus(sellerUuid, ReconciliationStatus.RECONCILED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, ReconciliationStatus.PENDING.name()),
                repository.countBySellerIdAndStatus(sellerUuid, ReconciliationStatus.REJECTED.name()),
                repository.countBySellerIdAndStatus(sellerUuid, ReconciliationStatus.CANCELLED.name()),
                repository.sumNetSettlementBySellerId(sellerUuid).defaultIfEmpty(0.0),
                repository.sumDiscrepancyAmountBySellerId(sellerUuid).defaultIfEmpty(0.0)
        ).map(tuple -> ReconciliationStatistics.builder()
                .totalReconciliations(tuple.getT1())
                .reconciledCount(tuple.getT2())
                .pendingCount(tuple.getT3())
                .rejectedCount(tuple.getT4())
                .cancelledCount(tuple.getT5())
                .totalNetSettlement(toBigDecimal(tuple.getT6()))
                .totalDiscrepancyAmount(toBigDecimal(tuple.getT7()))
                .calculatedAt(LocalDateTime.now())
                .build()
        );
    }

    @Override
    public Mono<ReconciliationStatistics> getReconciliationStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        UUID sellerUuid = UUID.fromString(sellerId.toString());

        return Mono.zip(
                repository.countBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate),
                repository.sumNetSettlementBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0),
                repository.sumDiscrepancyAmountBySellerIdAndCreatedAtBetween(sellerUuid, startDate, endDate).defaultIfEmpty(0.0)
        ).map(tuple -> ReconciliationStatistics.builder()
                .totalReconciliations(tuple.getT1())
                .totalNetSettlement(toBigDecimal(tuple.getT2()))
                .totalDiscrepancyAmount(toBigDecimal(tuple.getT3()))
                .calculatedAt(LocalDateTime.now())
                .build()
        );
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
