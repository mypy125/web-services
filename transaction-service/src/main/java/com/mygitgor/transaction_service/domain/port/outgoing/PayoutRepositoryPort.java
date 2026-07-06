package com.mygitgor.transaction_service.domain.port.outgoing;

import com.mygitgor.transaction_service.domain.model.Payout;
import com.mygitgor.transaction_service.domain.model.PayoutStatistics;
import com.mygitgor.transaction_service.domain.model.valueobject.PayoutStatus;
import com.mygitgor.transaction_service.shared.valueobject.PayoutId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface PayoutRepositoryPort {
    Mono<Payout> save(Payout payout);
    Mono<Payout> updateStatus(PayoutId payoutId, PayoutStatus status);
    Mono<Payout> completePayout(PayoutId payoutId, String gatewayTransactionId);
    Mono<Payout> failPayout(PayoutId payoutId, String reason);
    Mono<Void> deleteById(PayoutId payoutId);

    Mono<Payout> findById(PayoutId payoutId);
    Mono<Payout> findByReferenceNumber(String referenceNumber);
    Flux<Payout> findBySellerId(SellerId sellerId, int page, int size);
    Flux<Payout> findBySellerIdAndStatus(SellerId sellerId, PayoutStatus status, int page, int size);
    Flux<Payout> findBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );
    Flux<Payout> findByStatus(PayoutStatus status, int page, int size);
    Flux<Payout> findByIds(List<PayoutId> payoutIds);
    Mono<Long> countBySellerId(SellerId sellerId);
    Mono<Long> countBySellerIdAndStatus(SellerId sellerId, PayoutStatus status);
    Mono<Long> countBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<Long> countPendingPayouts();
    Mono<Double> sumAmountBySellerId(SellerId sellerId);
    Mono<Double> sumAmountBySellerIdAndStatus(SellerId sellerId, PayoutStatus status);
    Mono<Double> sumAmountBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Mono<PayoutStatistics> getPayoutStatistics(SellerId sellerId);
    Mono<PayoutStatistics> getPayoutStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
