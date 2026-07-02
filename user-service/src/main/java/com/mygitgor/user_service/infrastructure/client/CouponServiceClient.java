package com.mygitgor.user_service.infrastructure.client;

import com.mygitgor.user_service.domain.port.outgoing.CouponPort;
import com.mygitgor.user_service.infrastructure.client.exception.CouponServiceException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.user_service.infrastructure.client.fallback.CouponServiceFallback;
import com.mygitgor.user_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import com.mygitgor.user_service.application.dto.external.CouponDto;
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
public class CouponServiceClient implements CouponPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final CouponServiceFallback fallback;

    @Value("${coupon.service.url:http://localhost:8085/internal/coupons}")
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
                .defaultIfEmpty("Unknown error")
                .map(errorBody -> {
                    int statusCode = response.statusCode().value();
                    return switch (statusCode) {
                        case 404 -> CouponServiceException.notFound(identifier);
                        case 400 -> CouponServiceException.invalid(identifier);
                        case 409 -> CouponServiceException.alreadyUsed(identifier);
                        default -> new CouponServiceException(operation, statusCode,
                                "Client error: " + errorBody);
                    };
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Coupon Service", operation));
    }

    private Mono<Throwable> handleTimeoutError(String operation, String identifier, Throwable cause) {
        log.error("Timeout during {} for {}: {}", operation, identifier, cause.getMessage());
        return Mono.just(new ServiceTimeoutException("Coupon Service", operation, timeout));
    }

    @Override
    @CircuitBreaker(name = "couponService", fallbackMethod = "getUserCouponsFallback")
    @Retry(name = "couponService")
    @TimeLimiter(name = "couponService")
    public Flux<CouponDto> getUserCoupons(String userId) {
        log.debug("Fetching coupons for user: {}", userId);

        return webClient.get()
                .uri("/users/{userId}/coupons", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_USER_COUPONS", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_USER_COUPONS", userId))
                .bodyToFlux(CouponDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Coupon Service", "GET_USER_COUPONS", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnComplete(() -> log.debug("Coupons fetched for user: {}", userId))
                .doOnError(error -> log.error("Failed to fetch coupons for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "couponService", fallbackMethod = "markCouponAsUsedFallback")
    @Retry(name = "couponService")
    @TimeLimiter(name = "couponService")
    public Mono<Void> markCouponAsUsed(String userId, String couponCode) {
        log.info("Marking coupon as used for user: {}, coupon: {}", userId, couponCode);

        return webClient.post()
                .uri("/users/{userId}/coupons/{couponCode}/use", userId, couponCode)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "MARK_COUPON_USED", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "MARK_COUPON_USED", userId))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Coupon Service", "MARK_COUPON_USED", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Coupon marked as used for user: {}", userId))
                .doOnError(error -> log.error("Failed to mark coupon as used for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "couponService", fallbackMethod = "validateCouponFallback")
    @Retry(name = "couponService")
    @TimeLimiter(name = "couponService")
    public Mono<Boolean> validateCoupon(String userId, String couponCode) {
        log.debug("Validating coupon for user: {}, coupon: {}", userId, couponCode);

        return webClient.get()
                .uri("/users/{userId}/coupons/{couponCode}/validate", userId, couponCode)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "VALIDATE_COUPON", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "VALIDATE_COUPON", userId))
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Coupon Service", "VALIDATE_COUPON", timeout));
                    }
                    return Mono.error(e);
                })
                .defaultIfEmpty(false)
                .doOnSuccess(valid -> log.debug("Coupon {} is valid: {}", couponCode, valid))
                .doOnError(error -> log.error("Failed to validate coupon {} for user {}: {}", couponCode, userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "couponService", fallbackMethod = "getCouponDiscountFallback")
    @Retry(name = "couponService")
    @TimeLimiter(name = "couponService")
    public Mono<Double> getCouponDiscount(String userId, String couponCode) {
        log.debug("Getting discount for coupon: {}, user: {}", couponCode, userId);

        return webClient.get()
                .uri("/users/{userId}/coupons/{couponCode}/discount", userId, couponCode)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_COUPON_DISCOUNT", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_COUPON_DISCOUNT", userId))
                .bodyToMono(Double.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Coupon Service", "GET_COUPON_DISCOUNT", timeout));
                    }
                    return Mono.error(e);
                })
                .defaultIfEmpty(0.0)
                .doOnSuccess(discount -> log.debug("Coupon discount: {} for user: {}", discount, userId))
                .doOnError(error -> log.error("Failed to get coupon discount for user {}: {}", userId, error.getMessage()));
    }

    private Flux<CouponDto> getUserCouponsFallback(String userId, Throwable t) {
        log.warn("Fallback: getUserCoupons for user {} due to: {}", userId, t.getMessage());
        return fallback.getUserCoupons(userId);
    }

    private Mono<Void> markCouponAsUsedFallback(String userId, String couponCode, Throwable t) {
        log.warn("Fallback: markCouponAsUsed for user {} due to: {}", userId, t.getMessage());
        return fallback.markCouponAsUsed(userId, couponCode);
    }

    private Mono<Boolean> validateCouponFallback(String userId, String couponCode, Throwable t) {
        log.warn("Fallback: validateCoupon for user {} due to: {}", userId, t.getMessage());
        return fallback.validateCoupon(userId, couponCode);
    }

    private Mono<Double> getCouponDiscountFallback(String userId, String couponCode, Throwable t) {
        log.warn("Fallback: getCouponDiscount for user {} due to: {}", userId, t.getMessage());
        return fallback.getCouponDiscount(userId, couponCode);
    }
}
