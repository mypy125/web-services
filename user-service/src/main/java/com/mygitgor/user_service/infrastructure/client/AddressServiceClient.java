package com.mygitgor.user_service.infrastructure.client;

import com.mygitgor.user_service.domain.port.outgoing.AddressPort;
import com.mygitgor.user_service.infrastructure.client.exception.AddressServiceException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.user_service.infrastructure.client.fallback.AddressServiceFallback;
import com.mygitgor.user_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import com.mygitgor.user_service.application.dto.external.AddressDto;
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

import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressServiceClient implements AddressPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final AddressServiceFallback fallback;

    @Value("${address.service.url:http://localhost:8084/internal/addresses}")
    private String baseUrl;

    @Value("${address.service.timeout:5000}")
    private int timeout;

    @Value("${address.service.retry.attempts:3}")
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
                        case 404 -> AddressServiceException.notFound(identifier);
                        case 400 -> AddressServiceException.invalidRequest(identifier, errorBody);
                        case 409 -> AddressServiceException.conflict(identifier, errorBody);
                        default -> new AddressServiceException(operation, statusCode,
                                "Client error: " + errorBody);
                    };
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Address Service", operation));
    }

    private Mono<Throwable> handleTimeoutError(String operation, String identifier, Throwable cause) {
        log.error("Timeout during {} for {}: {}", operation, identifier, cause.getMessage());
        return Mono.just(new ServiceTimeoutException("Address Service", operation, timeout));
    }

    @Override
    @CircuitBreaker(name = "addressService", fallbackMethod = "getDefaultAddressFallback")
    @Retry(name = "addressService")
    @TimeLimiter(name = "addressService")
    public Mono<AddressDto> getDefaultAddress(String userId) {
        log.debug("Fetching default address for user: {}", userId);

        return webClient.get()
                .uri("/users/{userId}/default", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_DEFAULT_ADDRESS", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_DEFAULT_ADDRESS", userId))
                .bodyToMono(AddressDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Address Service", "GET_DEFAULT_ADDRESS", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(address -> {
                    if (address != null) {
                        log.debug("Default address fetched for user: {}", userId);
                    } else {
                        log.debug("No default address found for user: {}", userId);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch default address for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "addressService", fallbackMethod = "updateDefaultAddressFallback")
    @Retry(name = "addressService")
    @TimeLimiter(name = "addressService")
    public Mono<Void> updateDefaultAddress(String userId, String addressId) {
        log.info("Updating default address for user: {} to {}", userId, addressId);

        return webClient.patch()
                .uri("/users/{userId}/default/{addressId}", userId, addressId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "UPDATE_DEFAULT_ADDRESS", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "UPDATE_DEFAULT_ADDRESS", userId))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Address Service", "UPDATE_DEFAULT_ADDRESS", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Default address updated for user: {} to {}", userId, addressId))
                .doOnError(error -> log.error("Failed to update default address for user {}: {}", userId, error.getMessage()));
    }


    private Mono<AddressDto> getDefaultAddressFallback(String userId, Throwable t) {
        log.warn("Fallback: getDefaultAddress for user {} due to: {}", userId, t.getMessage());
        return fallback.getDefaultAddress(userId);
    }

    private Mono<Void> updateDefaultAddressFallback(String userId, String addressId, Throwable t) {
        log.warn("Fallback: updateDefaultAddress for user {} due to: {}", userId, t.getMessage());
        return fallback.updateDefaultAddress(userId, addressId);
    }
}
