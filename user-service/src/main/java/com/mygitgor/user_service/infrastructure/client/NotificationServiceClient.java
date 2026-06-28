package com.mygitgor.user_service.infrastructure.client;

import com.mygitgor.user_service.domain.port.outgoing.NotificationPort;
import com.mygitgor.user_service.infrastructure.client.dto.*;
import com.mygitgor.user_service.infrastructure.client.exception.NotificationServiceException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.user_service.infrastructure.client.fallback.NotificationServiceFallback;
import com.mygitgor.user_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import com.mygitgor.user_service.shared.valueobject.Email;
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
public class NotificationServiceClient implements NotificationPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final NotificationServiceFallback fallback;

    @Value("${notification.service.url:http://localhost:8085/internal/notifications}")
    private String baseUrl;

    @Value("${notification.service.timeout:5000}")
    private int timeout;

    @Value("${notification.service.retry.attempts:3}")
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
                        case 404 -> NotificationServiceException.templateNotFound(identifier);
                        case 400 -> NotificationServiceException.invalidRequest(identifier, errorBody);
                        case 429 -> NotificationServiceException.rateLimited(identifier);
                        default -> new NotificationServiceException(operation, statusCode,
                                "Client error: " + errorBody);
                    };
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Notification Service", operation));
    }

    private Mono<Throwable> handleTimeoutError(String operation, String identifier, Throwable cause) {
        log.error("Timeout during {} for {}: {}", operation, identifier, cause.getMessage());
        return Mono.just(new ServiceTimeoutException("Notification Service", operation, timeout));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendWelcomeEmailFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendWelcomeEmail(Email email, String name) {
        log.info("Sending welcome email to: {}", email);

        WelcomeEmailRequest request = new WelcomeEmailRequest(
                email.toString(),
                name
        );

        return webClient.post()
                .uri("/email/welcome")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "SEND_WELCOME_EMAIL", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "SEND_WELCOME_EMAIL", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Notification Service", "SEND_WELCOME_EMAIL", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Welcome email sent successfully to: {}", email))
                .doOnError(error -> log.error("Failed to send welcome email to: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendEmailVerifiedNotificationFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendEmailVerifiedNotification(Email email) {
        log.info("Sending email verified notification to: {}", email);

        EmailVerifiedRequest request = new EmailVerifiedRequest(
                email.toString()
        );

        return webClient.post()
                .uri("/email/verified")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "SEND_EMAIL_VERIFIED", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "SEND_EMAIL_VERIFIED", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Notification Service", "SEND_EMAIL_VERIFIED", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Email verified notification sent to: {}", email))
                .doOnError(error -> log.error("Failed to send email verified notification to: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendPasswordChangedNotificationFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendPasswordChangedNotification(Email email) {
        log.info("Sending password changed notification to: {}", email);

        PasswordChangedRequest request = new PasswordChangedRequest(
                email.toString()
        );

        return webClient.post()
                .uri("/email/password-changed")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "SEND_PASSWORD_CHANGED", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "SEND_PASSWORD_CHANGED", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Notification Service", "SEND_PASSWORD_CHANGED", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Password changed notification sent to: {}", email))
                .doOnError(error -> log.error("Failed to send password changed notification to: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendAccountActivatedNotificationFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendAccountActivatedNotification(Email email) {
        log.info("Sending account activated notification to: {}", email);

        AccountStatusChangeRequest request = new AccountStatusChangeRequest(
                email.toString(),
                "ACTIVATED"
        );

        return webClient.post()
                .uri("/email/account-activated")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "SEND_ACCOUNT_ACTIVATED", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "SEND_ACCOUNT_ACTIVATED", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Notification Service", "SEND_ACCOUNT_ACTIVATED", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Account activated notification sent to: {}", email))
                .doOnError(error -> log.error("Failed to send account activated notification to: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendAccountBannedNotificationFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendAccountBannedNotification(Email email, String reason) {
        log.info("Sending account banned notification to: {}, reason: {}", email, reason);

        AccountBannedRequest request = new AccountBannedRequest(
                email.toString(),
                reason
        );

        return webClient.post()
                .uri("/email/account-banned")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "SEND_ACCOUNT_BANNED", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "SEND_ACCOUNT_BANNED", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Notification Service", "SEND_ACCOUNT_BANNED", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Account banned notification sent to: {}", email))
                .doOnError(error -> log.error("Failed to send account banned notification to: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendAccountSuspendedNotificationFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendAccountSuspendedNotification(Email email, String reason) {
        log.info("Sending account suspended notification to: {}, reason: {}", email, reason);

        AccountSuspendedRequest request = new AccountSuspendedRequest(
                email.toString(),
                reason
        );

        return webClient.post()
                .uri("/email/account-suspended")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "SEND_ACCOUNT_SUSPENDED", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "SEND_ACCOUNT_SUSPENDED", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Notification Service", "SEND_ACCOUNT_SUSPENDED", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Account suspended notification sent to: {}", email))
                .doOnError(error -> log.error("Failed to send account suspended notification to: {}", email, error.getMessage()));
    }


    private Mono<Void> sendWelcomeEmailFallback(Email email, String name, Throwable t) {
        log.warn("Fallback: sendWelcomeEmail for {} due to: {}", email, t.getMessage());
        return fallback.sendWelcomeEmail(email, name);
    }

    private Mono<Void> sendEmailVerifiedNotificationFallback(Email email, Throwable t) {
        log.warn("Fallback: sendEmailVerifiedNotification for {} due to: {}", email, t.getMessage());
        return fallback.sendEmailVerifiedNotification(email);
    }

    private Mono<Void> sendPasswordChangedNotificationFallback(Email email, Throwable t) {
        log.warn("Fallback: sendPasswordChangedNotification for {} due to: {}", email, t.getMessage());
        return fallback.sendPasswordChangedNotification(email);
    }

    private Mono<Void> sendAccountActivatedNotificationFallback(Email email, Throwable t) {
        log.warn("Fallback: sendAccountActivatedNotification for {} due to: {}", email, t.getMessage());
        return fallback.sendAccountActivatedNotification(email);
    }

    private Mono<Void> sendAccountBannedNotificationFallback(Email email, String reason, Throwable t) {
        log.warn("Fallback: sendAccountBannedNotification for {} due to: {}", email, t.getMessage());
        return fallback.sendAccountBannedNotification(email, reason);
    }

    private Mono<Void> sendAccountSuspendedNotificationFallback(Email email, String reason, Throwable t) {
        log.warn("Fallback: sendAccountSuspendedNotification for {} due to: {}", email, t.getMessage());
        return fallback.sendAccountSuspendedNotification(email, reason);
    }
}
