package com.mygitgor.seller_service.infrastructure.client;

import com.mygitgor.seller_service.application.dto.external.ProductDetailsDto;
import com.mygitgor.seller_service.application.dto.external.ProductStatisticsDto;
import com.mygitgor.seller_service.application.dto.external.ProductSummaryDto;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.port.outgoing.ProductPort;
import com.mygitgor.seller_service.infrastructure.client.exception.ProductServiceException;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.seller_service.infrastructure.client.fallback.ProductServiceFallback;
import com.mygitgor.seller_service.infrastructure.client.intercepter.ServiceClientInterceptor;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient implements ProductPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final ProductServiceFallback fallback;

    @Value("${product.service.url:http://localhost:8088/api/products}")
    private String baseUrl;

    @Value("${product.service.timeout:5000}")
    private int timeout;

    @Value("${product.service.retry.attempts:3}")
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
                .map(errorBody -> switch (response.statusCode().value()) {
                    case 404 -> ProductServiceException.notFound(identifier);
                    case 400 -> ProductServiceException.invalidRequest(identifier, errorBody);
                    case 403 -> ProductServiceException.accessDenied(identifier);
                    default -> new ProductServiceException(operation, response.statusCode().value(), "Client error: " + errorBody);
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Product Service", operation));
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductDetailsFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Mono<ProductDetailsDto> getProductDetails(ProductId productId) {
        log.debug("Getting product details for: {}", productId);

        return webClient.get()
                .uri("/{productId}/details", productId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_PRODUCT_DETAILS", productId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_PRODUCT_DETAILS", productId.toString()))
                .bodyToMono(ProductDetailsDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Product Service", "GET_PRODUCT_DETAILS", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(details -> log.debug("Product details fetched: {}", productId))
                .doOnError(error -> log.error("Failed to get product details: {}", productId, error));
    }

    @Override
    public Flux<ProductSummaryDto> getProductsBySellerId(SellerId sellerId, int page, int size) {
        log.debug("Getting products for seller: {}, page: {}, size: {}", sellerId, page, size);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/products")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .retrieve()
                .bodyToFlux(ProductSummaryDto.class)
                .onErrorResume(e -> {
                    log.warn("Failed to get products for seller: {}", sellerId, e);
                    return Flux.empty();
                });
    }

    @Override
    public Flux<ProductSummaryDto> getActiveProductsBySellerId(SellerId sellerId, int page, int size) {
        log.debug("Getting active products for seller: {}", sellerId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/products/active")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .retrieve()
                .bodyToFlux(ProductSummaryDto.class)
                .onErrorResume(e -> {
                    log.warn("Failed to get active products for seller: {}", sellerId, e);
                    return Flux.empty();
                });
    }

    @Override
    public Mono<ProductStatisticsDto> getProductStatistics(SellerId sellerId) {
        log.debug("Getting product statistics for seller: {}", sellerId);
        return webClient.get()
                .uri("/sellers/{sellerId}/statistics", sellerId.toString())
                .retrieve()
                .bodyToMono(ProductStatisticsDto.class)
                .onErrorResume(e -> {
                    log.warn("Failed to get product statistics for seller: {}", sellerId, e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Long> countProductsBySellerId(SellerId sellerId) {
        return webClient.get()
                .uri("/sellers/{sellerId}/count", sellerId.toString())
                .retrieve()
                .bodyToMono(Long.class)
                .onErrorReturn(0L);
    }

    @Override
    public Mono<Long> countActiveProductsBySellerId(SellerId sellerId) {
        return webClient.get()
                .uri("/sellers/{sellerId}/count/active", sellerId.toString())
                .retrieve()
                .bodyToMono(Long.class)
                .onErrorReturn(0L);
    }

    @Override
    public Mono<Boolean> hasProducts(SellerId sellerId) {
        return countProductsBySellerId(sellerId)
                .map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> isProductBelongsToSeller(ProductId productId, SellerId sellerId) {
        return getProductDetails(productId)
                .map(product -> product.sellerId().equalsIgnoreCase(sellerId.toString()))
                .onErrorReturn(false);
    }

    @Override
    public Flux<ProductSummaryDto> getTopProductsBySellerId(SellerId sellerId, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/products/top")
                        .queryParam("limit", limit)
                        .build(sellerId.toString()))
                .retrieve()
                .bodyToFlux(ProductSummaryDto.class)
                .onErrorResume(e -> {
                    log.warn("Failed to get top products for seller: {}", sellerId, e);
                    return Flux.empty();
                });
    }

    private Mono<ProductDetailsDto> getProductDetailsFallback(ProductId productId, Throwable t) {
        log.warn("Fallback: getProductDetails for {} due to: {}", productId, t.getMessage());
        return fallback.getProductDetails(productId);
    }
}