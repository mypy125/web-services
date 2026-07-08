package com.mygitgor.seller_service.infrastructure.client;

import com.mygitgor.seller_service.domain.model.Coupon;
import com.mygitgor.seller_service.domain.port.outgoing.CouponPort;
import com.mygitgor.seller_service.infrastructure.client.exception.CouponServiceException;
import com.mygitgor.seller_service.infrastructure.client.fallback.CouponServiceFallback;
import com.mygitgor.seller_service.infrastructure.client.intercepter.ServiceClientInterceptor;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponServiceClient implements CouponPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final CouponServiceFallback fallback;

    @Value("${coupon.service.url:http://localhost:8088/api/v1/coupons}")
    private String baseUrl;

    @Value("${coupon.service.timeout:5000}")
    private int timeout;

    @Value("${coupon.service.retry.attempts:3}")
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
                .defaultIfEmpty("Unknown coupon client error")
                .map(errorBody -> switch (response.statusCode().value()) {
                    case 400 -> CouponServiceException.invalidCouponRequest(identifier, errorBody);
                    case 403 -> CouponServiceException.accessDenied(identifier);
                    case 404 -> CouponServiceException.couponNotFound(identifier);
                    case 409 -> CouponServiceException.couponConflict(identifier, errorBody);
                    default -> new CouponServiceException(operation, response.statusCode().value(), "Client error: " + errorBody);
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(CouponServiceException.unavailable(operation));
    }


    @Override
    @CircuitBreaker(name = "couponService", fallbackMethod = "createCouponFallback")
    @Retry(name = "couponService")
    @TimeLimiter(name = "couponService")
    public Mono<Coupon> createCoupon(Coupon coupon) {
        log.debug("Creating new coupon for seller: {}", coupon.getSellerId());

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(coupon)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "CREATE_COUPON", coupon.getSellerId().toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "CREATE_COUPON", coupon.getSellerId().toString()))
                .bodyToMono(Coupon.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(CouponServiceException.timeout("CREATE_COUPON"));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof CouponServiceException &&
                                ((CouponServiceException) throwable).getStatusCode() == 503))
                .doOnSuccess(savedCoupon -> log.debug("Coupon successfully created with code: {}", savedCoupon.getCode()))
                .doOnError(error -> log.error("Failed to create coupon for seller: {}", coupon.getSellerId(), error));
    }

    @Override
    @CircuitBreaker(name = "couponService", fallbackMethod = "getCouponsBySellerIdFallback")
    @Retry(name = "couponService")
    @TimeLimiter(name = "couponService")
    public Flux<Coupon> getCouponsBySellerId(SellerId sellerId, int page, int size) {
        log.debug("Getting coupons list for seller: {}, page: {}, size: {}", sellerId, page, size);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_COUPONS", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_COUPONS", sellerId.toString()))
                .bodyToFlux(Coupon.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Flux.error(CouponServiceException.timeout("GET_COUPONS"));
                    }
                    return Flux.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof CouponServiceException &&
                                ((CouponServiceException) throwable).getStatusCode() == 503))
                .doOnComplete(() -> log.debug("Coupons stream successfully completed for seller: {}", sellerId))
                .doOnError(error -> log.error("Failed to get coupons stream for seller: {}", sellerId, error));
    }

    private Mono<Coupon> createCouponFallback(Coupon coupon, Throwable t) {
        return fallback.createCouponFallback(coupon, t);
    }

    private Flux<Coupon> getCouponsBySellerIdFallback(SellerId sellerId, int page, int size, Throwable t) {
        return fallback.getCouponsBySellerIdFallback(sellerId, page, size, t);
    }
}
