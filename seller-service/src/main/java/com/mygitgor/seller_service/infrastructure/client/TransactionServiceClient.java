package com.mygitgor.seller_service.infrastructure.client;

import com.mygitgor.seller_service.application.dto.external.TransactionDto;
import com.mygitgor.seller_service.application.dto.external.TransactionStatisticsDto;
import com.mygitgor.seller_service.application.dto.request.CreateCommissionCommand;
import com.mygitgor.seller_service.application.dto.request.CreateRefundCommand;
import com.mygitgor.seller_service.application.dto.request.CreateSaleCommand;
import com.mygitgor.seller_service.domain.port.outgoing.TransactionPort;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceClientException;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.seller_service.infrastructure.client.exception.TransactionServiceException;
import com.mygitgor.seller_service.infrastructure.client.fallback.TransactionServiceFallback;
import com.mygitgor.seller_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionServiceClient implements TransactionPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final TransactionServiceFallback fallback;

    @Value("${transaction.service.url:http://localhost:8089/api/transactions}")
    private String baseUrl;

    @Value("${transaction.service.timeout:5000}")
    private int timeout;

    @Value("${transaction.service.retry.attempts:3}")
    private int retryAttempts;

    private WebClient webClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @PostConstruct
    private void init() {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .filter(clientInterceptor.logRequest())
                .filter(clientInterceptor.logResponse())
                .filter(clientInterceptor.handleErrors())
                .build();
    }

    private Mono<Throwable> handleClientErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Client error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Unknown transaction client error")
                .map(errorBody -> switch (response.statusCode().value()) {
                    case 400 -> TransactionServiceException.invalidTransactionRequest(operation, errorBody);
                    case 403 -> TransactionServiceException.accessDenied(identifier);
                    case 404 -> TransactionServiceException.transactionNotFound(identifier);
                    case 409 -> TransactionServiceException.stateConflict(identifier, errorBody);
                    default -> new TransactionServiceException(operation, response.statusCode().value(), "Client error: " + errorBody);
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(TransactionServiceException.unavailable(operation));
    }


    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsBySellerIdAndDateBetweenFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getTransactionsBySellerIdAndDateBetween(SellerId sellerId,
                                                                        LocalDateTime startDate,
                                                                        LocalDateTime endDate,
                                                                        int page, int size
    ) {
        log.debug("Getting transactions for seller: {}, from: {} to: {}", sellerId, startDate, endDate);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/date-range")
                        .queryParam("startDate", startDate.format(DATE_FORMATTER))
                        .queryParam("endDate", endDate.format(DATE_FORMATTER))
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_TRANSACTIONS_DATE_RANGE", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_TRANSACTIONS_DATE_RANGE", sellerId.toString()))
                .bodyToFlux(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Flux.error(new ServiceTimeoutException("Transaction Service", "GET_TRANSACTIONS_DATE_RANGE", (long) timeout, e));
                    }
                    return Flux.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(t -> t instanceof ServiceClientException && ((ServiceClientException) t).getStatusCode() == 503));
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionStatisticsFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Mono<TransactionStatisticsDto> getTransactionStatistics(SellerId sellerId) {
        log.debug("Getting transaction statistics for seller: {}", sellerId);

        return webClient.get()
                .uri("/sellers/{sellerId}/statistics", sellerId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_STATS", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_STATS", sellerId.toString()))
                .bodyToMono(TransactionStatisticsDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Transaction Service", "GET_STATS", (long) timeout, e));
                    }
                    return Mono.error(e);
                });
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionStatisticsWithDatesFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Mono<TransactionStatisticsDto> getTransactionStatistics(SellerId sellerId,
                                                                   LocalDateTime startDate,
                                                                   LocalDateTime endDate
    ) {
        log.debug("Getting transaction statistics for seller: {}, from: {} to: {}", sellerId, startDate, endDate);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/statistics/date-range")
                        .queryParam("startDate", startDate.format(DATE_FORMATTER))
                        .queryParam("endDate", endDate.format(DATE_FORMATTER))
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_STATS_DATES", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_STATS_DATES", sellerId.toString()))
                .bodyToMono(TransactionStatisticsDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Transaction Service", "GET_STATS_DATES", (long) timeout, e));
                    }
                    return Mono.error(e);
                });
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionByOrderIdFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Mono<TransactionDto> getTransactionByOrderId(OrderId orderId) {
        log.debug("Getting transaction by order ID: {}", orderId);

        return webClient.get()
                .uri("/orders/{orderId}", orderId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_BY_ORDER", orderId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_BY_ORDER", orderId.toString()))
                .bodyToMono(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Transaction Service", "GET_BY_ORDER", (long) timeout, e));
                    }
                    return Mono.error(e);
                });
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsBySellerIdFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getTransactionsBySellerId(SellerId sellerId, int page, int size) {
        log.debug("Getting transactions for seller: {}", sellerId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_SELLER_TXS", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_SELLER_TXS", sellerId.toString()))
                .bodyToFlux(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Flux.error(new ServiceTimeoutException("Transaction Service", "GET_SELLER_TXS", (long) timeout, e));
                    }
                    return Flux.error(e);
                });
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "createSaleFallback")
    public Mono<TransactionDto> createSaleTransaction(SellerId sellerId, CreateSaleCommand command) {
        log.debug("Sending command to create sale transaction for seller: {}, order: {}", sellerId, command.orderId());
        return postMutation("/sellers/{sellerId}/sales", sellerId.toString(), command, "CREATE_SALE");
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "createRefundFallback")
    public Mono<TransactionDto> createRefundTransaction(SellerId sellerId, CreateRefundCommand command) {
        log.debug("Sending command to create refund transaction for seller: {}, order: {}", sellerId, command.orderId());
        return postMutation("/sellers/{sellerId}/refunds", sellerId.toString(), command, "CREATE_REFUND");
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "createCommissionFallback")
    public Mono<TransactionDto> createCommissionTransaction(SellerId sellerId, CreateCommissionCommand command) {
        log.debug("Sending command to create commission transaction for seller: {}, order: {}", sellerId, command.orderId());
        return postMutation("/sellers/{sellerId}/commissions", sellerId.toString(), command, "CREATE_COMMISSION");
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "txIdFallback")
    public Mono<TransactionDto> completeTransaction(String transactionId) {
        log.debug("Sending request to complete transaction: {}", transactionId);
        return postMutation("/{transactionId}/complete", transactionId, null, "COMPLETE_TX");
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "failTransactionFallback")
    public Mono<TransactionDto> failTransaction(String transactionId, String reason) {
        log.debug("Sending request to fail transaction: {}, reason: {}", transactionId, reason);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/{transactionId}/fail")
                        .queryParam("reason", reason)
                        .build(transactionId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "FAIL_TX", transactionId))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "FAIL_TX", transactionId))
                .bodyToMono(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Transaction Service", "FAIL_TX", (long) timeout, e));
                    }
                    return Mono.error(e);
                });
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "txIdFallback")
    public Mono<TransactionDto> refundTransaction(String transactionId) {
        log.debug("Sending request to process direct refund for transaction ID: {}", transactionId);
        return postMutation("/{transactionId}/refund", transactionId, null, "REFUND_TX");
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "txIdFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Mono<TransactionDto> getTransactionById(String transactionId) {
        log.debug("Fetching transaction by ID: {}", transactionId);

        return webClient.get()
                .uri("/{transactionId}", transactionId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_BY_ID", transactionId))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_BY_ID", transactionId))
                .bodyToMono(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Transaction Service", "GET_BY_ID", (long) timeout, e));
                    }
                    return Mono.error(e);
                });
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "txIdFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Mono<TransactionDto> getTransactionByReferenceNumber(String referenceNumber) {
        log.debug("Fetching transaction by reference number: {}", referenceNumber);

        return webClient.get()
                .uri("/reference/{ref}", referenceNumber)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_BY_REF", referenceNumber))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_BY_REF", referenceNumber))
                .bodyToMono(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Transaction Service", "GET_BY_REF", (long) timeout, e));
                    }
                    return Mono.error(e);
                });
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsFilteredFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getTransactionsBySellerIdAndType(SellerId sellerId, String type, int page, int size) {
        log.debug("Fetching transactions for seller: {} filtered by type: {}", sellerId, type);
        return getFilteredFlux("/sellers/{sellerId}/type", sellerId.toString(), "type", type, page, size, "GET_SELLER_TXS_BY_TYPE");
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsFilteredFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getTransactionsBySellerIdAndStatus(SellerId sellerId, String status, int page, int size) {
        log.debug("Fetching transactions for seller: {} filtered by status: {}", sellerId, status);
        return getFilteredFlux("/sellers/{sellerId}/status", sellerId.toString(), "status", status, page, size, "GET_SELLER_TXS_BY_STATUS");
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsMultiFilteredFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getTransactionsBySellerIdAndTypeAndStatus(SellerId sellerId, String type, String status, int page, int size) {
        log.debug("Fetching transactions for seller: {} multi-filtered by type: {} and status: {}", sellerId, type, status);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/filter")
                        .queryParam("type", type)
                        .queryParam("status", status)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_SELLER_TXS_MULTI", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_SELLER_TXS_MULTI", sellerId.toString()))
                .bodyToFlux(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Flux.error(new ServiceTimeoutException("Transaction Service", "GET_SELLER_TXS_MULTI", (long) timeout, e));
                    }
                    return Flux.error(e);
                });
    }

    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsByIdsFallback")
    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService")
    public Flux<TransactionDto> getTransactionsByIds(List<String> transactionIds) {
        log.debug("Fetching batch transactions for IDs size: {}", transactionIds.size());

        return webClient.post()
                .uri("/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transactionIds)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_TXS_BATCH", "batch"))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_TXS_BATCH", "batch"))
                .bodyToFlux(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Flux.error(new ServiceTimeoutException("Transaction Service", "GET_TXS_BATCH", (long) timeout, e));
                    }
                    return Flux.error(e);
                });
    }

    private Mono<TransactionDto> postMutation(String path, String uriVar, Object body, String operation) {
        WebClient.RequestBodySpec spec = webClient.post().uri(path, uriVar);
        if (body != null) {
            spec.bodyValue(body);
        }
        return spec.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, uriVar))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, uriVar))
                .bodyToMono(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Transaction Service", operation, (long) timeout, e));
                    }
                    return Mono.error(e);
                });
    }

    private Flux<TransactionDto> getFilteredFlux(String path, String sellerId, String paramName, String paramValue, int page, int size, String operation) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam(paramName, paramValue)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, sellerId))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, sellerId))
                .bodyToFlux(TransactionDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Flux.error(new ServiceTimeoutException("Transaction Service", operation, (long) timeout, e));
                    }
                    return Flux.error(e);
                });
    }


    private Flux<TransactionDto> getTransactionsBySellerIdAndDateBetweenFallback(SellerId s, LocalDateTime start, LocalDateTime end, int p, int sz, Throwable t) {
        return fallback.getTransactionsFallback(s, start, end, p, sz, t);
    }

    private Flux<TransactionDto> getTransactionsBySellerIdFallback(SellerId s, int p, int sz, Throwable t) {
        return fallback.getTransactionsSimpleFallback(s, p, sz, t);
    }

    private Mono<TransactionStatisticsDto> getTransactionStatisticsFallback(SellerId s, Throwable t) {
        return fallback.getStatisticsFallback(s, t);
    }

    private Mono<TransactionStatisticsDto> getTransactionStatisticsWithDatesFallback(SellerId s, LocalDateTime start, LocalDateTime end, Throwable t) {
        return fallback.getStatisticsFallback(s, t);
    }

    private Mono<TransactionDto> getTransactionByOrderIdFallback(OrderId o, Throwable t) {
        return fallback.getTransactionByOrderIdFallback(o, t);
    }

    private Mono<TransactionDto> createSaleFallback(SellerId s, CreateSaleCommand c, Throwable t) {
        return fallback.mutationFallback("CREATE_SALE", t);
    }

    private Mono<TransactionDto> createRefundFallback(SellerId s, CreateRefundCommand c, Throwable t) {
        return fallback.mutationFallback("CREATE_REFUND", t);
    }

    private Mono<TransactionDto> createCommissionFallback(SellerId s, CreateCommissionCommand c, Throwable t) {
        return fallback.mutationFallback("CREATE_COMMISSION", t);
    }

    private Mono<TransactionDto> txIdFallback(String txId, Throwable t) {
        return fallback.mutationFallback("MUTATE_TX_ID", t);
    }

}
