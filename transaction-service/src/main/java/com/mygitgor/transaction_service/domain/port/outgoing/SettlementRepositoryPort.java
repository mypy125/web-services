package com.mygitgor.transaction_service.domain.port.outgoing;

import com.mygitgor.transaction_service.domain.model.Settlement;
import com.mygitgor.transaction_service.domain.model.SettlementStatistics;
import com.mygitgor.transaction_service.domain.model.valueobject.SettlementStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.SettlementType;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.SettlementId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementRepositoryPort {
    Mono<Settlement> save(Settlement settlement);
    Mono<Settlement> updateStatus(SettlementId settlementId, SettlementStatus status);
    Mono<Settlement> processSettlement(SettlementId settlementId, String processedBy);
    Mono<Settlement> completeSettlement(SettlementId settlementId, String gatewayTransactionId);
    Mono<Settlement> settleSettlement(SettlementId settlementId);
    Mono<Settlement> failSettlement(SettlementId settlementId, String reason);
    Mono<Void> deleteById(SettlementId settlementId);
    Mono<Settlement> findById(SettlementId settlementId);
    Mono<Settlement> findByReferenceNumber(String referenceNumber);
    Flux<Settlement> findBySellerId(SellerId sellerId, int page, int size);
    Flux<Settlement> findBySellerIdAndStatus(SellerId sellerId, SettlementStatus status, int page, int size);
    Flux<Settlement> findBySellerIdAndType(SellerId sellerId, SettlementType type, int page, int size);
    Flux<Settlement> findBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );
    Flux<Settlement> findByStatus(SettlementStatus status, int page, int size);
    Flux<Settlement> findByIds(List<SettlementId> settlementIds);
    Mono<Long> countBySellerId(SellerId sellerId);
    Mono<Long> countBySellerIdAndStatus(SellerId sellerId, SettlementStatus status);
    Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<Long> countPendingSettlements();
    Mono<Double> sumAmountBySellerId(SellerId sellerId);
    Mono<Double> sumAmountBySellerIdAndStatus(SellerId sellerId, SettlementStatus status);
    Mono<Double> sumAmountBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Mono<Double> sumNetAmountBySellerId(SellerId sellerId);
    Mono<SettlementStatistics> getSettlementStatistics(SellerId sellerId);
    Mono<SettlementStatistics> getSettlementStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
