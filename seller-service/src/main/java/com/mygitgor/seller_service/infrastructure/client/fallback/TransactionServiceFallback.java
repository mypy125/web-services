package com.mygitgor.seller_service.infrastructure.client.fallback;

import com.mygitgor.seller_service.application.dto.external.TransactionDto;
import com.mygitgor.seller_service.application.dto.external.TransactionStatisticsDto;
import com.mygitgor.seller_service.application.dto.request.CreateCommissionCommand;
import com.mygitgor.seller_service.application.dto.request.CreateRefundCommand;
import com.mygitgor.seller_service.application.dto.request.CreateSaleCommand;
import com.mygitgor.seller_service.infrastructure.client.exception.TransactionServiceException;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
public class TransactionServiceFallback {

    public Flux<TransactionDto> getTransactionsFallback(SellerId sellerId, LocalDateTime start, LocalDateTime end, int page, int size, Throwable t) {
        log.warn("Fallback: Failed to fetch date-range transactions for seller: {}. Returning empty list.", sellerId);
        return Flux.empty();
    }

    public Flux<TransactionDto> getTransactionsSimpleFallback(SellerId sellerId, int page, int size, Throwable t) {
        log.warn("Fallback: Failed to fetch transactions for seller: {}. Returning empty list.", sellerId);
        return Flux.empty();
    }

    public Mono<TransactionStatisticsDto> getStatisticsFallback(SellerId sellerId, Throwable t) {
        log.warn("Fallback: Failed to fetch transaction stats for seller: {} due to: {}. Returning zeroed metrics.",
                sellerId, t.getMessage());

        return Mono.just(TransactionStatisticsDto.builder()
                .totalTransactions(0L)
                .completedTransactions(0L)
                .pendingTransactions(0L)
                .failedTransactions(0L)
                .refundedTransactions(0L)
                .cancelledTransactions(0L)
                .totalAmount(0.0)
                .totalCommission(0.0)
                .totalRefunds(0.0)
                .totalNetAmount(0.0)
                .averageTransactionAmount(0.0)
                .calculatedAt(LocalDateTime.now())
                .build());
    }

    public Mono<TransactionDto> getTransactionByOrderIdFallback(OrderId orderId, Throwable t) {
        log.warn("Fallback: Failed to fetch transaction for order: {}.", orderId);
        return Mono.error(TransactionServiceException.unavailable("GET_TRANSACTION_BY_ORDER"));
    }

    public Mono<TransactionDto> createSaleFallback(SellerId sellerId, CreateSaleCommand command, Throwable t) {
        log.error("Fallback triggered for CREATE_SALE for seller: {}, order: {}. Reason: {}",
                sellerId, command.orderId(), t.getMessage());
        return Mono.error(TransactionServiceException.unavailable("CREATE_SALE"));
    }

    public Mono<TransactionDto> createRefundFallback(SellerId sellerId, CreateRefundCommand command, Throwable t) {
        log.error("Fallback triggered for CREATE_REFUND for seller: {}, order: {}. Reason: {}",
                sellerId, command.orderId(), t.getMessage());
        return Mono.error(TransactionServiceException.unavailable("CREATE_REFUND"));
    }

    public Mono<TransactionDto> createCommissionFallback(SellerId sellerId, CreateCommissionCommand command, Throwable t) {
        log.error("Fallback triggered for CREATE_COMMISSION for seller: {}, order: {}. Reason: {}",
                sellerId, command.orderId(), t.getMessage());
        return Mono.error(TransactionServiceException.unavailable("CREATE_COMMISSION"));
    }

    public Mono<TransactionDto> mutationFallback(String operation, Throwable t) {
        log.error("Fallback: Structural mutation [{}] failed directly due to service outage. Reason: {}", operation, t.getMessage());
        return Mono.error(TransactionServiceException.unavailable(operation));
    }
}
