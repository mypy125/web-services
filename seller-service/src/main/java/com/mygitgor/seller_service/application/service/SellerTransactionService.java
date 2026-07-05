package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.external.TransactionDto;
import com.mygitgor.seller_service.application.dto.external.TransactionStatisticsDto;
import com.mygitgor.seller_service.application.dto.request.CreateCommissionCommand;
import com.mygitgor.seller_service.application.dto.request.CreateRefundCommand;
import com.mygitgor.seller_service.application.dto.request.CreateSaleCommand;
import com.mygitgor.seller_service.domain.model.status.TransactionStatus;
import com.mygitgor.seller_service.domain.model.type.TransactionType;
import com.mygitgor.seller_service.domain.port.outgoing.TransactionPort;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.infrastructure.kafka.producer.SellerEventProducer;
import com.mygitgor.seller_service.shared.exception.DomainException;
import com.mygitgor.seller_service.shared.exception.SellerNotFoundException;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
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
public class SellerTransactionService {
    private final TransactionPort transactionPort;
    private final SellerRepositoryPort sellerRepository;
    private final SellerEventProducer eventProducer;

    public Mono<TransactionDto> createSaleTransaction(SellerId sellerId, CreateSaleCommand command) {
        log.info("Creating sale transaction for seller: {}, order: {}", sellerId, command.orderId());

        return validateSellerCanCreateTransaction(sellerId)
                .then(Mono.defer(() -> transactionPort.createSaleTransaction(sellerId, command)))
                .delayUntil(eventProducer::sendTransactionCreatedEvent)
                .doOnSuccess(dto -> log.info("Sale transaction successfully processed by remote service: {}", dto.transactionId()))
                .doOnError(error -> log.error("Failed to create remote sale transaction: {}", error.getMessage()));
    }

    public Mono<TransactionDto> createRefundTransaction(SellerId sellerId, CreateRefundCommand command) {
        log.info("Creating refund transaction for seller: {}, order: {}, amount: {}", sellerId, command.orderId(), command.amount());

        return validateSellerCanCreateTransaction(sellerId)
                .then(Mono.defer(() -> transactionPort.createRefundTransaction(sellerId, command)))
                .delayUntil(eventProducer::sendTransactionCreatedEvent)
                .doOnSuccess(dto -> log.info("Refund transaction successfully processed by remote service: {}", dto.transactionId()))
                .doOnError(error -> log.error("Failed to create remote refund transaction: {}", error.getMessage()));
    }

    public Mono<TransactionDto> createCommissionTransaction(SellerId sellerId, CreateCommissionCommand command) {
        log.info("Creating commission transaction for seller: {}, order: {}", sellerId, command.orderId(), command.amount());

        return validateSellerCanCreateTransaction(sellerId)
                .then(Mono.defer(() -> transactionPort.createCommissionTransaction(sellerId, command)))
                .delayUntil(eventProducer::sendTransactionCreatedEvent)
                .doOnSuccess(dto -> log.info("Commission transaction successfully processed: {}", dto.transactionId()));
    }

    public Mono<TransactionDto> completeTransaction(String transactionId) {
        log.info("Completing transaction: {}", transactionId);

        return transactionPort.completeTransaction(transactionId)
                .delayUntil(eventProducer::sendTransactionCompletedEvent)
                .doOnSuccess(dto -> log.info("Remote transaction marked as completed: {}", transactionId));
    }

    public Mono<TransactionDto> failTransaction(String transactionId, String reason) {
        log.info("Failing transaction: {}, reason: {}", transactionId, reason);

        return transactionPort.failTransaction(transactionId, reason)
                .delayUntil(eventProducer::sendTransactionFailedEvent)
                .doOnSuccess(dto -> log.info("Remote transaction marked as failed: {}", transactionId));
    }

    public Mono<TransactionDto> refundTransaction(String transactionId) {
        log.info("Refunding transaction: {}", transactionId);

        return transactionPort.refundTransaction(transactionId)
                .delayUntil(eventProducer::sendTransactionRefundedEvent)
                .doOnSuccess(dto -> log.info("Remote transaction marked as refunded: {}", transactionId));
    }

    public Mono<TransactionDto> getTransactionById(String transactionId) {
        log.debug("Getting transaction by ID: {}", transactionId);
        return transactionPort.getTransactionById(transactionId);
    }

    public Mono<TransactionDto> getTransactionByOrderId(OrderId orderId) {
        log.debug("Getting transaction by order ID: {}", orderId);
        return transactionPort.getTransactionByOrderId(orderId);
    }

    public Flux<TransactionDto> getTransactionsBySellerId(SellerId sellerId, int page, int size) {
        return transactionPort.getTransactionsBySellerId(sellerId, page, size);
    }

    public Flux<TransactionDto> getTransactionsBySellerIdAndType(SellerId sellerId, String type, int page, int size) {
        return transactionPort.getTransactionsBySellerIdAndType(sellerId, type, page, size);
    }

    public Flux<TransactionDto> getTransactionsBySellerIdAndStatus(SellerId sellerId, String status, int page, int size) {
        return transactionPort.getTransactionsBySellerIdAndStatus(sellerId, status, page, size);
    }

    public Flux<TransactionDto> getTransactionsBySellerIdAndDateBetween(
            SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size
    ) {
        return transactionPort.getTransactionsBySellerIdAndDateBetween(sellerId, startDate, endDate, page, size);
    }

    public Flux<TransactionDto> getAllTransactionsBySellerId(SellerId sellerId) {
        log.warn("Pulling all transactions for seller {} without batching limits!", sellerId);
        return transactionPort.getTransactionsBySellerId(sellerId, 0, 5000);
    }

    public Flux<TransactionDto> getTransactionsBySellerIdAndTypeAndStatus(
            SellerId sellerId, String type, String status, int page, int size
    ) {
        return transactionPort.getTransactionsBySellerIdAndTypeAndStatus(sellerId, type, status, page, size);
    }

    public Mono<TransactionStatisticsDto> getTransactionStatistics(SellerId sellerId) {
        return transactionPort.getTransactionStatistics(sellerId);
    }

    public Mono<TransactionStatisticsDto> getTransactionStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionPort.getTransactionStatistics(sellerId, startDate, endDate);
    }

    public Mono<Double> getTotalAmountBySellerId(SellerId sellerId) {
        return transactionPort.getTransactionStatistics(sellerId).map(TransactionStatisticsDto::totalAmount);
    }

    public Mono<Double> getTotalAmountBySellerIdAndType(SellerId sellerId) {
        return transactionPort.getTransactionStatistics(sellerId).map(TransactionStatisticsDto::totalCommission);
    }

    public Mono<Long> getTotalCommissionBySellerId(SellerId sellerId) {
        return transactionPort.getTransactionStatistics(sellerId).map(TransactionStatisticsDto::totalTransactions);
    }

    public Mono<Long> countTransactionsBySellerId(SellerId sellerId) {
        return transactionPort.getTransactionStatistics(sellerId).map(TransactionStatisticsDto::totalTransactions);
    }

    public Flux<TransactionDto> getTransactionsByIds(List<String> transactionIds) {
        return transactionPort.getTransactionsByIds(transactionIds);
    }

    public Mono<TransactionDto> getTransactionByReferenceNumber(String referenceNumber) {
        return transactionPort.getTransactionByReferenceNumber(referenceNumber);
    }

    private Mono<Void> validateSellerCanCreateTransaction(SellerId sellerId) {
        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> {
                    if (!seller.canSell()) {
                        return Mono.error(new DomainException("Transaction blocked: Seller account status does not permit active sales operations"));
                    }
                    return Mono.empty();
                });
    }

    public Flux<TransactionDto> getTransactionsByDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        return transactionPort.getTransactionsBySellerIdAndDateBetween(
                sellerId, startDate, endDate, page, size
        );
    }
}