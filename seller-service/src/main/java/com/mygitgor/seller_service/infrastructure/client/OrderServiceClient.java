package com.mygitgor.seller_service.infrastructure.client;

import com.mygitgor.seller_service.application.dto.external.OrderDetailsDto;
import com.mygitgor.seller_service.application.dto.external.OrderStatisticsDto;
import com.mygitgor.seller_service.application.dto.external.OrderSummaryDto;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.port.outgoing.OrderPort;
import com.mygitgor.seller_service.infrastructure.client.exception.OrderServiceException;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.seller_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import com.mygitgor.seller_service.infrastructure.client.fallback.OrderServiceFallback;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
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
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient implements OrderPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final OrderServiceFallback fallback;

    @Value("${order.service.url:http://localhost:8086/api/orders}")
    private String baseUrl;

    @Value("${order.service.timeout:5000}")
    private int timeout;

    @Value("${order.service.retry.attempts:3}")
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
                .defaultIfEmpty("Unknown error")
                .map(errorBody -> switch (response.statusCode().value()) {
                    case 404 -> OrderServiceException.orderNotFound(identifier);
                    case 400 -> OrderServiceException.invalidRequest(identifier, errorBody);
                    case 403 -> OrderServiceException.accessDenied(identifier);
                    case 409 -> OrderServiceException.conflict(identifier, errorBody);
                    default -> new OrderServiceException(operation, response.statusCode().value(), "Client error: " + errorBody);
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Order Service", operation));
    }

    @Override
    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderDetailsFallback")
    @Retry(name = "orderService")
    @TimeLimiter(name = "orderService")
    public Mono<OrderDetailsDto> getOrderDetails(OrderId orderId) {
        log.debug("Getting order details for: {}", orderId);

        return webClient.get()
                .uri("/{orderId}/details", orderId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_ORDER_DETAILS", orderId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_ORDER_DETAILS", orderId.toString()))
                .bodyToMono(OrderDetailsDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Order Service", "GET_ORDER_DETAILS", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(details -> log.debug("Order details fetched: {}", orderId))
                .doOnError(error -> log.error("Failed to get order details: {}", orderId, error));
    }

    @Override
    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrdersBySellerIdFallback")
    @Retry(name = "orderService")
    @TimeLimiter(name = "orderService")
    public Flux<OrderSummaryDto> getOrdersBySellerId(SellerId sellerId, int page, int size) {
        log.debug("Getting orders for seller: {}, page: {}, size: {}", sellerId, page, size);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/orders")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_ORDERS_BY_SELLER", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_ORDERS_BY_SELLER", sellerId.toString()))
                .bodyToFlux(OrderSummaryDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Order Service", "GET_ORDERS_BY_SELLER", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnComplete(() -> log.debug("Orders fetched for seller: {}", sellerId))
                .doOnError(error -> log.error("Failed to get orders for seller: {}", sellerId, error));
    }

    @Override
    public Flux<OrderSummaryDto> getOrdersBySellerIdAndDateBetween(SellerId sellerId,
                                                                   LocalDateTime startDate,
                                                                   LocalDateTime endDate,
                                                                   int page, int size) {
        log.debug("Getting orders for seller: {}, from: {} to: {}", sellerId, startDate, endDate);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/orders/date-range")
                        .queryParam("startDate", startDate.format(DATE_FORMATTER))
                        .queryParam("endDate", endDate.format(DATE_FORMATTER))
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_ORDERS_BY_DATE", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_ORDERS_BY_DATE", sellerId.toString()))
                .bodyToFlux(OrderSummaryDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Order Service", "GET_ORDERS_BY_DATE", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable));
    }

    @Override
    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderStatisticsFallback")
    @Retry(name = "orderService")
    @TimeLimiter(name = "orderService")
    public Mono<OrderStatisticsDto> getOrderStatistics(SellerId sellerId) {
        log.debug("Getting order statistics for seller: {}", sellerId);

        return webClient.get()
                .uri("/sellers/{sellerId}/statistics", sellerId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_ORDER_STATISTICS", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_ORDER_STATISTICS", sellerId.toString()))
                .bodyToMono(OrderStatisticsDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Order Service", "GET_ORDER_STATISTICS", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(stats -> log.debug("Order statistics fetched for seller: {}", sellerId))
                .doOnError(error -> log.error("Failed to get order statistics for seller: {}", sellerId, error));
    }

    @Override
    public Mono<OrderStatisticsDto> getOrderStatistics(SellerId sellerId,
                                                       LocalDateTime startDate,
                                                       LocalDateTime endDate) {
        log.debug("Getting order statistics for seller: {}, from: {} to: {}", sellerId, startDate, endDate);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/statistics/date-range")
                        .queryParam("startDate", startDate.format(DATE_FORMATTER))
                        .queryParam("endDate", endDate.format(DATE_FORMATTER))
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_ORDER_STATISTICS_BY_DATE", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_ORDER_STATISTICS_BY_DATE", sellerId.toString()))
                .bodyToMono(OrderStatisticsDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Order Service", "GET_ORDER_STATISTICS_BY_DATE", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable));
    }

    @Override
    public Mono<Long> countOrdersBySellerId(SellerId sellerId) {
        log.debug("Counting orders for seller: {}", sellerId);
        return webClient.get()
                .uri("/sellers/{sellerId}/count", sellerId.toString())
                .retrieve()
                .bodyToMono(Long.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorReturn(0L);
    }

    @Override
    public Mono<Long> countOrdersBySellerIdAndDateBetween(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Counting orders for seller: {}, from: {} to: {}", sellerId, startDate, endDate);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/count/date-range")
                        .queryParam("startDate", startDate.format(DATE_FORMATTER))
                        .queryParam("endDate", endDate.format(DATE_FORMATTER))
                        .build(sellerId.toString()))
                .retrieve()
                .bodyToMono(Long.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorReturn(0L);
    }

    @Override
    public Mono<Boolean> hasOrders(SellerId sellerId) {
        return countOrdersBySellerId(sellerId)
                .map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> hasOrders(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return countOrdersBySellerIdAndDateBetween(sellerId, startDate, endDate)
                .map(count -> count > 0);
    }

    private Mono<OrderDetailsDto> getOrderDetailsFallback(OrderId orderId, Throwable t) {
        log.warn("Fallback: getOrderDetails for {} due to: {}", orderId, t.getMessage());
        return fallback.getOrderDetails(orderId);
    }

    private Flux<OrderSummaryDto> getOrdersBySellerIdFallback(SellerId sellerId, int page, int size, Throwable t) {
        log.warn("Fallback: getOrdersBySellerId for {} due to: {}", sellerId, t.getMessage());
        return fallback.getOrdersBySellerId(sellerId, page, size);
    }

    private Mono<OrderStatisticsDto> getOrderStatisticsFallback(SellerId sellerId, Throwable t) {
        log.warn("Fallback: getOrderStatistics for {} due to: {}", sellerId, t.getMessage());
        return fallback.getOrderStatistics(sellerId);
    }
}