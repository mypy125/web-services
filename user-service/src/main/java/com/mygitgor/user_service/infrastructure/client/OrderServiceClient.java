package com.mygitgor.user_service.infrastructure.client;

import com.mygitgor.user_service.domain.port.outgoing.OrderPort;
import com.mygitgor.user_service.infrastructure.client.exception.OrderServiceException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.user_service.infrastructure.client.fallback.OrderServiceFallback;
import com.mygitgor.user_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import com.mygitgor.user_service.infrastructure.dto.external.OrderStatisticsDto;
import com.mygitgor.user_service.infrastructure.dto.external.OrderSummaryDto;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient implements OrderPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final OrderServiceFallback fallback;

    @Value("${order.service.url:http://localhost:8086/internal/orders}")
    private String baseUrl;

    @Value("${order.service.timeout:5000}")
    private int timeout;

    @Value("${order.service.retry.attempts:3}")
    private int retryAttempts;

    private WebClient webClient;

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
                .map(errorBody -> {
                    int statusCode = response.statusCode().value();
                    return switch (statusCode) {
                        case 404 -> OrderServiceException.notFound(identifier);
                        case 400 -> OrderServiceException.invalidRequest(identifier, errorBody);
                        case 403 -> OrderServiceException.accessDenied(identifier);
                        case 409 -> OrderServiceException.conflict(identifier, errorBody);
                        default -> new OrderServiceException(operation, statusCode,
                                "Client error: " + errorBody);
                    };
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Order Service", operation));
    }

    private Mono<Throwable> handleTimeoutError(String operation, String identifier, Throwable cause) {
        log.error("Timeout during {} for {}: {}", operation, identifier, cause.getMessage());
        return Mono.just(new ServiceTimeoutException("Order Service", operation, timeout));
    }

    @Override
    @CircuitBreaker(name = "orderService", fallbackMethod = "getUserOrdersFallback")
    @Retry(name = "orderService")
    @TimeLimiter(name = "orderService")
    public Flux<OrderSummaryDto> getUserOrders(String userId, int page, int size) {
        log.debug("Fetching orders for user: {}, page: {}, size: {}", userId, page, size);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/{userId}/orders")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(userId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_USER_ORDERS", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_USER_ORDERS", userId))
                .bodyToFlux(OrderSummaryDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Order Service", "GET_USER_ORDERS", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnComplete(() -> log.debug("Orders fetched for user: {}", userId))
                .doOnError(error -> log.error("Failed to fetch orders for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderStatisticsFallback")
    @Retry(name = "orderService")
    @TimeLimiter(name = "orderService")
    public Mono<OrderStatisticsDto> getOrderStatistics(String userId) {
        log.debug("Fetching order statistics for user: {}", userId);

        return webClient.get()
                .uri("/users/{userId}/statistics", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_ORDER_STATISTICS", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_ORDER_STATISTICS", userId))
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
                .doOnSuccess(stats -> {
                    if (stats != null) {
                        log.debug("Order statistics fetched for user: {}", userId);
                    } else {
                        log.debug("No order statistics found for user: {}", userId);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch order statistics for user {}: {}", userId, error.getMessage()));
    }


    private Flux<OrderSummaryDto> getUserOrdersFallback(String userId, int page, int size, Throwable t) {
        log.warn("Fallback: getUserOrders for user {} due to: {}", userId, t.getMessage());
        return fallback.getUserOrders(userId, page, size);
    }

    private Mono<OrderStatisticsDto> getOrderStatisticsFallback(String userId, Throwable t) {
        log.warn("Fallback: getOrderStatistics for user {} due to: {}", userId, t.getMessage());
        return fallback.getOrderStatistics(userId);
    }
}
