package com.mygitgor.user_service.infrastructure.client;

import com.mygitgor.user_service.domain.port.outgoing.PaymentPort;
import com.mygitgor.user_service.infrastructure.client.exception.PaymentServiceException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.user_service.infrastructure.client.fallback.PaymentServiceFallback;
import com.mygitgor.user_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import com.mygitgor.user_service.infrastructure.dto.external.PaymentMethodDto;
import reactor.core.publisher.Mono;
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

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceClient implements PaymentPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final PaymentServiceFallback fallback;

    @Value("${payment.service.url:http://localhost:8087/internal/payments}")
    private String baseUrl;

    @Value("${payment.service.timeout:5000}")
    private int timeout;

    @Value("${payment.service.retry.attempts:3}")
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
                        case 404 -> switch (operation) {
                            case "GET_DEFAULT_PAYMENT_METHOD" -> PaymentServiceException.defaultMethodNotFound(identifier);
                            case "GET_USER_PAYMENT_METHODS" -> PaymentServiceException.noPaymentMethods(identifier);
                            default -> PaymentServiceException.notFound(identifier);
                        };
                        case 400 -> switch (operation) {
                            case "ADD_PAYMENT_METHOD" -> PaymentServiceException.addFailed(identifier, errorBody);
                            case "UPDATE_DEFAULT_PAYMENT_METHOD" -> PaymentServiceException.updateDefaultFailed(identifier, "unknown_method_id");
                            case "DELETE_PAYMENT_METHOD" -> PaymentServiceException.deleteFailed(identifier, "unknown_method_id");
                            default -> PaymentServiceException.invalidRequest(identifier, errorBody);
                        };
                        case 403 -> PaymentServiceException.accessDenied(identifier);
                        case 409 -> PaymentServiceException.conflict(identifier, errorBody);
                        case 402 -> PaymentServiceException.insufficientFunds(identifier);
                        default -> new PaymentServiceException(operation, statusCode, "Client error: " + errorBody);
                    };
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Payment Service", operation));
    }

    private Mono<Throwable> handleTimeoutError(String operation, String identifier, Throwable cause) {
        log.error("Timeout during {} for {}: {}", operation, identifier, cause.getMessage());
        return Mono.just(new ServiceTimeoutException("Payment Service", operation, timeout));
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getDefaultPaymentMethodFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public Mono<PaymentMethodDto> getDefaultPaymentMethod(String userId) {
        log.debug("Fetching default payment method for user: {}", userId);

        return webClient.get()
                .uri("/users/{userId}/payment-methods/default", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_DEFAULT_PAYMENT_METHOD", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_DEFAULT_PAYMENT_METHOD", userId))
                .bodyToMono(PaymentMethodDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Payment Service", "GET_DEFAULT_PAYMENT_METHOD", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(paymentMethod -> {
                    if (paymentMethod != null) {
                        log.debug("Default payment method fetched for user: {}", userId);
                    } else {
                        log.debug("No default payment method found for user: {}", userId);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch default payment method for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getUserPaymentMethodsFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public Mono<List<PaymentMethodDto>> getUserPaymentMethods(String userId) {
        log.debug("Fetching all payment methods for user: {}", userId);

        return webClient.get()
                .uri("/users/{userId}/payment-methods", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_USER_PAYMENT_METHODS", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_USER_PAYMENT_METHODS", userId))
                .bodyToFlux(PaymentMethodDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Payment Service", "GET_USER_PAYMENT_METHODS", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .collectList()
                .doOnSuccess(list -> log.debug("Payment methods fetched for user: {}, count: {}", userId, list.size()))
                .doOnError(error -> log.error("Failed to fetch payment methods for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "addPaymentMethodFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public Mono<PaymentMethodDto> addPaymentMethod(String userId, PaymentMethodDto paymentMethod) {
        log.info("Adding payment method for user: {}", userId);

        return webClient.post()
                .uri("/users/{userId}/payment-methods", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(paymentMethod)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "ADD_PAYMENT_METHOD", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "ADD_PAYMENT_METHOD", userId))
                .bodyToMono(PaymentMethodDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Payment Service", "ADD_PAYMENT_METHOD", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(method -> log.info("Payment method added for user: {}", userId))
                .doOnError(error -> log.error("Failed to add payment method for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "updateDefaultPaymentMethodFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public Mono<PaymentMethodDto> updateDefaultPaymentMethod(String userId, String paymentMethodId) {
        log.info("Updating default payment method for user: {} to {}", userId, paymentMethodId);

        return webClient.patch()
                .uri("/users/{userId}/payment-methods/{paymentMethodId}/default", userId, paymentMethodId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "UPDATE_DEFAULT_PAYMENT_METHOD", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "UPDATE_DEFAULT_PAYMENT_METHOD", userId))
                .bodyToMono(PaymentMethodDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Payment Service", "UPDATE_DEFAULT_PAYMENT_METHOD", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(method -> log.info("Default payment method updated for user: {} to {}", userId, paymentMethodId))
                .doOnError(error -> log.error("Failed to update default payment method for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "deletePaymentMethodFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public Mono<Void> deletePaymentMethod(String userId, String paymentMethodId) {
        log.info("Deleting payment method for user: {}, method: {}", userId, paymentMethodId);

        return webClient.delete()
                .uri("/users/{userId}/payment-methods/{paymentMethodId}", userId, paymentMethodId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "DELETE_PAYMENT_METHOD", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "DELETE_PAYMENT_METHOD", userId))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Payment Service", "DELETE_PAYMENT_METHOD", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Payment method deleted for user: {}, method: {}", userId, paymentMethodId))
                .doOnError(error -> log.error("Failed to delete payment method for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "validatePaymentMethodFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public Mono<Boolean> validatePaymentMethod(String userId, String paymentMethodId) {
        log.debug("Validating payment method for user: {}, method: {}", userId, paymentMethodId);

        return webClient.get()
                .uri("/users/{userId}/payment-methods/{paymentMethodId}/validate", userId, paymentMethodId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "VALIDATE_PAYMENT_METHOD", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "VALIDATE_PAYMENT_METHOD", userId))
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Payment Service", "VALIDATE_PAYMENT_METHOD", timeout));
                    }
                    return Mono.error(e);
                })
                .defaultIfEmpty(false)
                .doOnSuccess(valid -> log.debug("Payment method {} is valid: {}", paymentMethodId, valid))
                .doOnError(error -> log.error("Failed to validate payment method for user {}: {}", userId, error.getMessage()));
    }

    private Mono<PaymentMethodDto> getDefaultPaymentMethodFallback(String userId, Throwable t) {
        log.warn("Fallback: getDefaultPaymentMethod for user {} due to: {}", userId, t.getMessage());
        return fallback.getDefaultPaymentMethod(userId);
    }

    private Flux<PaymentMethodDto> getUserPaymentMethodsFallback(String userId, Throwable t) {
        log.warn("Fallback: getUserPaymentMethods for user {} due to: {}", userId, t.getMessage());
        return fallback.getUserPaymentMethods(userId);
    }

    private Mono<PaymentMethodDto> addPaymentMethodFallback(String userId, PaymentMethodDto paymentMethod, Throwable t) {
        log.warn("Fallback: addPaymentMethod for user {} due to: {}", userId, t.getMessage());
        return fallback.addPaymentMethod(userId, paymentMethod);
    }

    private Mono<PaymentMethodDto> updateDefaultPaymentMethodFallback(String userId, String paymentMethodId, Throwable t) {
        log.warn("Fallback: updateDefaultPaymentMethod for user {} due to: {}", userId, t.getMessage());
        return fallback.updateDefaultPaymentMethod(userId, paymentMethodId);
    }

    private Mono<Void> deletePaymentMethodFallback(String userId, String paymentMethodId, Throwable t) {
        log.warn("Fallback: deletePaymentMethod for user {} due to: {}", userId, t.getMessage());
        return fallback.deletePaymentMethod(userId, paymentMethodId);
    }

    private Mono<Boolean> validatePaymentMethodFallback(String userId, String paymentMethodId, Throwable t) {
        log.warn("Fallback: validatePaymentMethod for user {} due to: {}", userId, t.getMessage());
        return fallback.validatePaymentMethod(userId, paymentMethodId);
    }
}
