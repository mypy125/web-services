package com.mygitgor.seller_service.domain.port.outgoing;

import com.mygitgor.seller_service.application.dto.external.TransactionDto;
import com.mygitgor.seller_service.application.dto.external.TransactionStatisticsDto;
import com.mygitgor.seller_service.application.dto.request.CreateCommissionCommand;
import com.mygitgor.seller_service.application.dto.request.CreateRefundCommand;
import com.mygitgor.seller_service.application.dto.request.CreateSaleCommand;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;
import java.util.List;

import reactor.core.publisher.Mono;

public interface TransactionPort {
    Flux<TransactionDto> getTransactionsBySellerIdAndDateBetween(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );
    Mono<TransactionStatisticsDto> getTransactionStatistics(SellerId sellerId);
    Mono<TransactionStatisticsDto> getTransactionStatistics(
            SellerId sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Mono<TransactionDto> getTransactionByOrderId(OrderId orderId);
    Flux<TransactionDto> getTransactionsBySellerId(SellerId sellerId, int page, int size);
    Mono<TransactionDto> createSaleTransaction(SellerId sellerId, CreateSaleCommand command);
    Mono<TransactionDto> createRefundTransaction(SellerId sellerId, CreateRefundCommand command);
    Mono<TransactionDto> createCommissionTransaction(SellerId sellerId, CreateCommissionCommand command);
    Mono<TransactionDto> completeTransaction(String transactionId);
    Mono<TransactionDto> failTransaction(String transactionId, String reason);
    Mono<TransactionDto> refundTransaction(String transactionId);
    Mono<TransactionDto> getTransactionById(String transactionId);
    Mono<TransactionDto> getTransactionByReferenceNumber(String referenceNumber);
    Flux<TransactionDto> getTransactionsBySellerIdAndType(SellerId sellerId, String type, int page, int size);
    Flux<TransactionDto> getTransactionsBySellerIdAndStatus(SellerId sellerId, String status, int page, int size);
    Flux<TransactionDto> getTransactionsBySellerIdAndTypeAndStatus(
            SellerId sellerId,
            String type,
            String status,
            int page,
            int size
    );
    Flux<TransactionDto> getTransactionsByIds(List<String> transactionIds);
}
