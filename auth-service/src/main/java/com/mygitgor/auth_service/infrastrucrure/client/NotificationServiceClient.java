package com.mygitgor.auth_service.infrastrucrure.client;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.port.NotificationPort;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.infrastrucrure.client.dto.EmailNotificationRequestDto;
import com.mygitgor.auth_service.infrastrucrure.client.exception.NotificationNotFoundException;
import com.mygitgor.auth_service.infrastrucrure.client.exception.ServiceClientException;
import com.mygitgor.auth_service.infrastrucrure.client.exception.ServiceUnavailableException;
import com.mygitgor.auth_service.infrastrucrure.client.fallback.NotificationServiceFallback;
import com.mygitgor.auth_service.infrastrucrure.client.interceptor.ServiceClientInterceptor;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

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
                    if (response.statusCode().value() == 404) {
                        return new NotificationNotFoundException("Notification service error: " + identifier);
                    }
                    if (response.statusCode().value() == 400) {
                        return new IllegalArgumentException("Bad request: " + errorBody);
                    }
                    return new ServiceClientException("Client error during " + operation + ": " + errorBody);
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Notification service unavailable during " + operation));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendOtpEmailFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendOtpEmail(Email email, String otp, OtpPurpose purpose) {
        log.info("Sending OTP email to: {}, purpose: {}", email, purpose);

        EmailNotificationRequestDto request = EmailNotificationRequestDto.builder()
                .to(email.toString())
                .subject("Your OTP Code")
                .templateName("otp-email")
                .templateData(Map.of(
                        "otp", otp,
                        "purpose", purpose.name(),
                        "expiresIn", "10 minutes",
                        "email", email.toString()
                ))
                .otp(otp)
                .purpose(purpose.name())
                .build();

        return webClient.post()
                .uri("/email/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "send OTP email", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "send OTP email", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("OTP email sent successfully to: {}", email))
                .doOnError(error -> log.error("Failed to send OTP email to {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendWelcomeEmailFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendWelcomeEmail(Email email, String name) {
        log.info("Sending welcome email to: {}", email);

        EmailNotificationRequestDto request = EmailNotificationRequestDto.builder()
                .to(email.toString())
                .subject("Welcome to Our Platform!")
                .templateName("welcome-email")
                .templateData(Map.of(
                        "name", name,
                        "email", email.toString(),
                        "loginUrl", "https://yourapp.com/login"
                ))
                .build();

        return webClient.post()
                .uri("/email/send-welcome")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "send welcome email", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "send welcome email", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Welcome email sent successfully to: {}", email))
                .doOnError(error -> log.error("Failed to send welcome email to {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendEmailVerifiedNotificationFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendEmailVerifiedNotification(Email email) {
        log.info("Sending email verified notification to: {}", email);

        EmailNotificationRequestDto request = EmailNotificationRequestDto.builder()
                .to(email.toString())
                .subject("Email Verified Successfully")
                .templateName("email-verified")
                .templateData(Map.of(
                        "email", email.toString(),
                        "verifiedAt", LocalDateTime.now().toString()
                ))
                .build();

        return webClient.post()
                .uri("/email/send-verification-success")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "send email verified notification", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "send email verified notification", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Email verified notification sent to: {}", email))
                .doOnError(error -> log.error("Failed to send email verified notification to {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendPasswordChangedNotificationFallback")
    @Retry(name = "notificationService")
    @TimeLimiter(name = "notificationService")
    public Mono<Void> sendPasswordChangedNotification(Email email) {
        log.info("Sending password changed notification to: {}", email);

        EmailNotificationRequestDto request = EmailNotificationRequestDto.builder()
                .to(email.toString())
                .subject("Password Changed Successfully")
                .templateName("password-changed")
                .templateData(Map.of(
                        "email", email.toString(),
                        "changedAt", LocalDateTime.now().toString(),
                        "supportEmail", "support@yourapp.com"
                ))
                .build();

        return webClient.post()
                .uri("/email/send-password-changed")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "send password changed notification", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "send password changed notification", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Password changed notification sent to: {}", email))
                .doOnError(error -> log.error("Failed to send password changed notification to {}: {}", email, error.getMessage()));
    }

    private Mono<Void> sendOtpEmailFallback(Email email, String otp, OtpPurpose purpose, Throwable t) {
        log.warn("Fallback: sendOtpEmail for {} due to: {}", email, t.getMessage());
        return fallback.sendOtpEmail(email, otp, purpose);
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
}
