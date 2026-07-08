package com.mygitgor.seller_service.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.seller_service.application.dto.response.ApiResponse;
import com.mygitgor.seller_service.application.dto.response.UserAuthInfoResponse;
import com.mygitgor.seller_service.domain.port.outgoing.AuthPort;
import com.mygitgor.seller_service.infrastructure.client.exception.AuthServiceException;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.seller_service.infrastructure.client.fallback.AuthServiceFallback;
import com.mygitgor.seller_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import com.mygitgor.seller_service.shared.valueobject.Email;
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
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceClient implements AuthPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final AuthServiceFallback fallback;
    private final ObjectMapper objectMapper;

    @Value("${auth.service.url:http://localhost:8081/api/v1/auth}")
    private String authServiceUrl;

    @Value("${auth.service.timeout:5000}")
    private int timeout;

    @Value("${auth.service.retry.attempts:3}")
    private int retryAttempts;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        this.webClient = webClientBuilder
                .baseUrl(authServiceUrl)
                .filter(clientInterceptor.logRequest())
                .filter(clientInterceptor.logResponse())
                .filter(clientInterceptor.handleErrors())
                .build();
    }

    private Mono<Throwable> handleClientErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Client error during {} for {}: Status={}", operation, identifier, response.statusCode());

        return response.bodyToMono(String.class)
                .defaultIfEmpty("Unknown auth client error")
                .map(errorBody -> switch (response.statusCode().value()) {
                    case 400 -> AuthServiceException.invalidOtp(identifier);
                    case 401 -> operation.equals("VALIDATE_TOKEN") ? AuthServiceException.invalidToken() : AuthServiceException.invalidCredentials(errorBody);
                    case 403 -> AuthServiceException.accessDenied();
                    default -> new AuthServiceException(operation, response.statusCode().value(), "Client error: " + errorBody);
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Auth Service", operation));
    }

    @Override
    @CircuitBreaker(name = "authService", fallbackMethod = "verifyOtpFallback")
    @Retry(name = "authService")
    @TimeLimiter(name = "authService")
    public Mono<Boolean> verifyOtp(Email email, String otp) {
        log.debug("Verifying OTP for email: {}", email);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/seller/verify")
                        .queryParam("email", email.toString())
                        .queryParam("otp", otp)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "VERIFY_OTP", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "VERIFY_OTP", email.toString()))
                .toBodilessEntity()
                .map(responseEntity -> responseEntity.getStatusCode().is2xxSuccessful())
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Auth Service", "VERIFY_OTP", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof ServiceUnavailableException))
                .doOnSuccess(success -> log.debug("OTP verification completed with status: {}", success))
                .doOnError(error -> log.error("Failed OTP verification for: {}", email, error));
    }


    @Override
    @CircuitBreaker(name = "authService", fallbackMethod = "validateTokenFallback")
    @Retry(name = "authService")
    @TimeLimiter(name = "authService")
    public Mono<Boolean> validateToken(String token) {
        log.debug("Validating token structure");

        return webClient.get()
                .uri("/validate")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "VALIDATE_TOKEN", "token"))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "VALIDATE_TOKEN", "token"))
                .bodyToMono(ApiResponse.class)
                .map(ApiResponse::isSuccess)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Auth Service", "VALIDATE_TOKEN", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof ServiceUnavailableException))
                .doOnSuccess(success -> log.debug("Token validation completed. Valid={}", success));
    }

    @Override
    @CircuitBreaker(name = "authService", fallbackMethod = "getUserInfoFromTokenFallback")
    @Retry(name = "authService")
    @TimeLimiter(name = "authService")
    public Mono<UserAuthInfoResponse> getUserInfoFromToken(String token) {
        log.debug("Fetching user profile details from token");

        return webClient.get()
                .uri("/user-info")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_USER_INFO", "token"))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_USER_INFO", "token"))
                .bodyToMono(ApiResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Auth Service", "GET_USER_INFO", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof ServiceUnavailableException))
                .map(response -> {
                    if (response == null || !response.isSuccess() || response.getData() == null) {
                        throw new AuthServiceException("GET_USER_INFO", 200, "Received empty or unsuccessful body from auth service");
                    }
                    return objectMapper.convertValue(response.getData(), UserAuthInfoResponse.class);
                })
                .doOnSuccess(info -> log.debug("User profile context fetched successfully"))
                .doOnError(error -> log.error("Failed to fetch user profile details from token", error));
    }

    private Mono<Boolean> verifyOtpFallback(Email email, String otp, Throwable t) {
        return fallback.verifyOtpFallback(email, otp, t);
    }

    private Mono<Boolean> validateTokenFallback(String token, Throwable t) {
        return fallback.validateTokenFallback(token, t);
    }

    private Mono<UserAuthInfoResponse> getUserInfoFromTokenFallback(String token, Throwable t) {
        return fallback.getUserInfoFromTokenFallback(token, t);
    }
}

