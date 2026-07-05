package com.mygitgor.seller_service.infrastructure.client;

import com.mygitgor.seller_service.application.dto.external.TransactionDto;
import com.mygitgor.seller_service.application.dto.external.TransactionStatisticsDto;
import com.mygitgor.seller_service.application.dto.request.CreateCommissionCommand;
import com.mygitgor.seller_service.application.dto.request.CreateRefundCommand;
import com.mygitgor.seller_service.application.dto.request.CreateSaleCommand;
import com.mygitgor.seller_service.domain.port.outgoing.TransactionPort;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionServiceClient implements TransactionPort {
    private final WebClient.Builder webClientBuilder;

    @Value("${transaction.service.url:http://localhost:8089/api/transactions}")
    private String baseUrl;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Flux<TransactionDto> getTransactionsBySellerIdAndDateBetween(SellerId sellerId,
                                                                        LocalDateTime startDate,
                                                                        LocalDateTime endDate,
                                                                        int page, int size
    ) {
        log.debug("Getting transactions for seller: {}, from: {} to: {}", sellerId, startDate, endDate);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/date-range")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                .onErrorResume(e -> {
                    log.error("Failed to get transactions: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Mono<TransactionStatisticsDto> getTransactionStatistics(SellerId sellerId) {
        log.debug("Getting transaction statistics for seller: {}", sellerId);
        return webClient.get()
                .uri("/sellers/{sellerId}/statistics", sellerId.toString())
                .retrieve()
                .bodyToMono(TransactionStatisticsDto.class);
    }

    @Override
    public Mono<TransactionStatisticsDto> getTransactionStatistics(SellerId sellerId,
                                                                   LocalDateTime startDate,
                                                                   LocalDateTime endDate
    ) {
        log.debug("Getting transaction statistics for seller: {}, from: {} to: {}", sellerId, startDate, endDate);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/statistics/date-range")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build(sellerId.toString()))
                .retrieve()
                .bodyToMono(TransactionStatisticsDto.class);
    }

    @Override
    public Mono<TransactionDto> getTransactionByOrderId(OrderId orderId) {
        log.debug("Getting transaction by order ID: {}", orderId);
        return webClient.get()
                .uri("/orders/{orderId}", orderId.toString())
                .retrieve()
                .bodyToMono(TransactionDto.class);
    }

    @Override
    public Flux<TransactionDto> getTransactionsBySellerId(SellerId sellerId, int page, int size) {
        log.debug("Getting transactions for seller: {}", sellerId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .retrieve()
                .bodyToFlux(TransactionDto.class);
    }

    @Override
    public Mono<TransactionDto> createSaleTransaction(SellerId sellerId, CreateSaleCommand command) {
        return null;
    }

    @Override
    public Mono<TransactionDto> createRefundTransaction(SellerId sellerId, CreateRefundCommand command) {
        return null;
    }

    @Override
    public Mono<TransactionDto> createCommissionTransaction(SellerId sellerId, CreateCommissionCommand command) {
        return null;
    }

    @Override
    public Mono<TransactionDto> completeTransaction(String transactionId) {
        return null;
    }

    @Override
    public Mono<TransactionDto> failTransaction(String transactionId, String reason) {
        return null;
    }

    @Override
    public Mono<TransactionDto> refundTransaction(String transactionId) {
        return null;
    }

    @Override
    public Mono<TransactionDto> getTransactionById(String transactionId) {
        return null;
    }

    @Override
    public Mono<TransactionDto> getTransactionByReferenceNumber(String referenceNumber) {
        return null;
    }

    @Override
    public Flux<TransactionDto> getTransactionsBySellerIdAndType(SellerId sellerId, String type, int page, int size) {
        return null;
    }

    @Override
    public Flux<TransactionDto> getTransactionsBySellerIdAndStatus(SellerId sellerId, String status, int page, int size) {
        return null;
    }

    @Override
    public Flux<TransactionDto> getTransactionsBySellerIdAndTypeAndStatus(SellerId sellerId, String type, String status, int page, int size) {
        return null;
    }

    @Override
    public Flux<TransactionDto> getTransactionsByIds(List<String> transactionIds) {
        return null;
    }
}
