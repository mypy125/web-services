package com.mygitgor.transaction_service.domain.port.outgoing;

import com.mygitgor.transaction_service.domain.model.Reconciliation;
import com.mygitgor.transaction_service.domain.model.ReconciliationStatistics;
import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationType;
import com.mygitgor.transaction_service.shared.valueobject.ReconciliationId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface ReconciliationRepositoryPort {
    Mono<Reconciliation> save(Reconciliation reconciliation);
    Mono<Reconciliation> updateStatus(ReconciliationId reconciliationId, ReconciliationStatus status);
    Mono<Reconciliation> reconcile(ReconciliationId reconciliationId, String resolvedBy);
    Mono<Reconciliation> reject(ReconciliationId reconciliationId, String reason);
    Mono<Void> deleteById(ReconciliationId reconciliationId);
    Mono<Reconciliation> findById(ReconciliationId reconciliationId);
    Mono<Reconciliation> findByPeriod(SellerId sellerId, String period);
    Flux<Reconciliation> findBySellerId(SellerId sellerId, int page, int size);
    Flux<Reconciliation> findBySellerIdAndStatus(SellerId sellerId, ReconciliationStatus status, int page, int size);
    Flux<Reconciliation> findBySellerIdAndType(SellerId sellerId, ReconciliationType type, int page, int size);
    Flux<Reconciliation> findBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );
    Flux<Reconciliation> findByStatus(ReconciliationStatus status, int page, int size);
    Flux<Reconciliation> findByIds(List<ReconciliationId> reconciliationIds);
    Mono<Long> countBySellerId(SellerId sellerId);
    Mono<Long> countBySellerIdAndStatus(SellerId sellerId, ReconciliationStatus status);
    Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<Long> countPendingReconciliations();
    Mono<ReconciliationStatistics> getReconciliationStatistics(SellerId sellerId);
    Mono<ReconciliationStatistics> getReconciliationStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
