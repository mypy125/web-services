package com.mygitgor.auth_service.infrastrucrure.client;

import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.user.model.User;
import com.mygitgor.auth_service.domain.user.model.UserStatistics;
import com.mygitgor.auth_service.domain.user.port.UserPort;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.client.dto.*;
import com.mygitgor.auth_service.infrastrucrure.client.exception.ServiceClientException;
import com.mygitgor.auth_service.infrastrucrure.client.exception.ServiceUnavailableException;
import com.mygitgor.auth_service.infrastrucrure.client.exception.UserAlreadyExistsException;
import com.mygitgor.auth_service.infrastrucrure.client.exception.UserNotFoundException;
import com.mygitgor.auth_service.infrastrucrure.client.fallback.UserServiceFallback;
import com.mygitgor.auth_service.infrastrucrure.client.interceptor.ServiceClientInterceptor;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient implements UserPort {

    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final UserServiceFallback fallback;

    @Value("${user.service.url:http://localhost:8082/api/users}")
    private String baseUrl;

    @Value("${user.service.timeout:5000}")
    private int timeout;

    @Value("${user.service.retry.attempts:3}")
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
                        return new UserNotFoundException("User not found: " + identifier);
                    }
                    if (response.statusCode().value() == 400) {
                        return new IllegalArgumentException("Bad request: " + errorBody);
                    }
                    if (response.statusCode().value() == 409) {
                        return new UserAlreadyExistsException("User already exists: " + identifier);
                    }
                    return new ServiceClientException("Client error during " + operation + ": " + errorBody);
                });
    }


    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("User service unavailable during " + operation));
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "existsByEmailFallback")
    @Retry(name = "userService")
    @TimeLimiter(name = "userService")
    public Mono<Boolean> existsByEmail(Email email) {
        log.debug("Checking if user exists by email: {}", email);

        return webClient.get()
                .uri("/exists/{email}", email.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "exists by email", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "exists by email", email.toString()))
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable ||
                                throwable instanceof java.net.ConnectException))
                .onErrorReturn(false)
                .doOnSuccess(result -> log.debug("User exists by email {}: {}", email, result))
                .doOnError(error -> log.error("Error checking user existence for {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    @Retry(name = "userService")
    @TimeLimiter(name = "userService")
    public Mono<User> getUserByEmail(Email email) {
        log.info("Fetching user by email: {}", email);

        return webClient.get()
                .uri("/{email}/auth-info", email.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "get user by email", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "get user by email", email.toString()))
                .bodyToMono(UserAuthInfoDto.class)
                .map(this::toDomainUser)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(user -> log.info("Successfully fetched user: {}", email))
                .doOnError(error -> log.error("Failed to fetch user by email {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "userService")
    @TimeLimiter(name = "userService")
    public Mono<User> getUserById(UserId userId) {
        log.debug("Fetching user by ID: {}", userId);

        return webClient.get()
                .uri("/id/{userId}", userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new UserNotFoundException("User not found with ID: " + userId)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "get user by id", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "get user by id", userId.toString()))
                .bodyToMono(UserDto.class)
                .map(this::toDomainUser)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(user -> log.debug("User fetched successfully: {}", userId))
                .doOnError(error -> log.error("Failed to fetch user by ID {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "createUserFallback")
    @Retry(name = "userService")
    @TimeLimiter(name = "userService")
    public Mono<User> createUser(User user) {
        log.info("Creating new user: {}", user.getEmail());

        UserCreateRequestDto request = UserCreateRequestDto.builder()
                .email(user.getEmail().toString())
                .fullName(user.getFullName())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .build();

        return webClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode.CONFLICT::equals, response ->
                        handleClientErrorResponse(response, "create user", user.getEmail().toString()))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "create user", user.getEmail().toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "create user", user.getEmail().toString()))
                .bodyToMono(UserDto.class)
                .map(this::toDomainUser)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(created -> log.info("User created successfully: {}", user.getEmail()))
                .doOnError(error -> log.error("Failed to create user {}: {}", user.getEmail(), error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "updateUserFallback")
    @Retry(name = "userService")
    @TimeLimiter(name = "userService")
    public Mono<User> updateUser(User user) {
        log.info("Updating user: {}", user.getEmail());

        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .build();

        return webClient.put()
                .uri("/{userId}", user.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new UserNotFoundException("User not found: " + user.getEmail())))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "update user", user.getEmail().toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "update user", user.getEmail().toString()))
                .bodyToMono(UserDto.class)
                .map(this::toDomainUser)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(updated -> log.info("User updated successfully: {}", user.getEmail()))
                .doOnError(error -> log.error("Failed to update user {}: {}", user.getEmail(), error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "verifyEmailFallback")
    @Retry(name = "userService")
    @TimeLimiter(name = "userService")
    public Mono<User> verifyEmail(Email email) {
        log.info("Verifying email for user: {}", email);

        return webClient.patch()
                .uri("/{email}/verify", email.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new UserNotFoundException("User not found: " + email)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "verify email", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "verify email", email.toString()))
                .bodyToMono(UserDto.class)
                .map(this::toDomainUser)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(verified -> log.info("Email verified successfully for user: {}", email))
                .doOnError(error -> log.error("Failed to verify email for user {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public Mono<Void> updateLastLogin(Email email, LocalDateTime lastLoginAt) {
        log.debug("Updating last login for user: {} at {}", email, lastLoginAt);

        return webClient.patch()
                .uri("/{email}/last-login", email.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("lastLoginAt", lastLoginAt))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "update last login", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "update last login", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .then()
                .doOnSuccess(v -> log.debug("Last login updated for user: {}", email))
                .doOnError(error -> log.warn("Failed to update last login for user {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public Mono<Boolean> isEmailVerified(Email email) {
        log.debug("Checking if email is verified for user: {}", email);

        return webClient.get()
                .uri("/{email}/email-verified", email.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "check email verified", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "check email verified", email.toString()))
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofMillis(timeout))
                .defaultIfEmpty(false)
                .doOnSuccess(verified -> log.debug("Email verified status for {}: {}", email, verified));
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public Mono<User> updateAccountStatus(Email email, String status) {
        log.info("Updating account status for user {} to {}", email, status);

        return webClient.patch()
                .uri("/{email}/status", email.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", status))
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new UserNotFoundException("User not found: " + email)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "update account status", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "update account status", email.toString()))
                .bodyToMono(UserDto.class)
                .map(this::toDomainUser)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(user -> log.info("Account status updated for user: {} to {}", email, status))
                .doOnError(error -> log.error("Failed to update account status for user {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public Mono<Void> changePassword(Email email, String newPassword) {
        log.info("Changing password for user: {}", email);

        return webClient.post()
                .uri("/{email}/change-password", email.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("password", newPassword))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "change password", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "change password", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .then()
                .doOnSuccess(v -> log.info("Password changed for user: {}", email))
                .doOnError(error -> log.error("Failed to change password for user {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public Mono<Void> deleteUser(Email email) {
        log.info("Deleting user: {}", email);

        return webClient.delete()
                .uri("/{email}", email.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "delete user", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "delete user", email.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .then()
                .doOnSuccess(v -> log.info("User deleted: {}", email))
                .doOnError(error -> log.error("Failed to delete user {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public Mono<UserStatistics> getUserStatistics(UserId userId) {
        log.debug("Fetching statistics for user: {}", userId);

        return webClient.get()
                .uri("/{userId}/statistics", userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "get user statistics", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "get user statistics", userId.toString()))
                .bodyToMono(UserStatisticsDto.class)
                .map(this::toDomainStatistics)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(stats -> log.debug("Statistics fetched for user: {}", userId))
                .doOnError(error -> log.warn("Failed to fetch statistics for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public Mono<Page<User>> searchUsers(String searchTerm, int page, int size) {
        log.debug("Searching users with term: {}, page: {}, size: {}", searchTerm, page, size);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("term", searchTerm)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ServiceClientException("Client error searching users")))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ServiceUnavailableException("User service unavailable")))
                .bodyToMono(UserPageDto.class)
                .map(this::toDomainPage)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(result -> log.debug("Search completed, found {} users",
                        result.getContent() != null ? result.getContent().size() : 0));
    }

    // Fallback methods
    private Mono<Boolean> existsByEmailFallback(Email email, Throwable t) {
        log.warn("Fallback: existsByEmail for {} due to: {}", email, t.getMessage());
        return fallback.existsByEmail(email);
    }

    private Mono<User> getUserByEmailFallback(Email email, Throwable t) {
        log.warn("Fallback: getUserByEmail for {} due to: {}", email, t.getMessage());
        return fallback.getUserByEmail(email);
    }

    private Mono<User> getUserByIdFallback(UserId userId, Throwable t) {
        log.warn("Fallback: getUserById for {} due to: {}", userId, t.getMessage());
        return fallback.getUserById(userId);
    }

    private Mono<User> createUserFallback(User user, Throwable t) {
        log.warn("Fallback: createUser for {} due to: {}", user.getEmail(), t.getMessage());
        return fallback.createUser(user);
    }

    private Mono<User> updateUserFallback(User user, Throwable t) {
        log.warn("Fallback: updateUser for {} due to: {}", user.getEmail(), t.getMessage());
        return fallback.updateUser(user);
    }

    private Mono<User> verifyEmailFallback(Email email, Throwable t) {
        log.warn("Fallback: verifyEmail for {} due to: {}", email, t.getMessage());
        return fallback.verifyEmail(email);
    }

    private User toDomainUser(UserDto dto) {
        if (dto == null) return null;

        return User.builder()
                .id(new UserId(dto.getId()))
                .email(new Email(dto.getEmail()))
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .emailVerified(dto.isEmailVerified())
                .profileImage(dto.getProfileImage())
                .phoneNumber(dto.getPhoneNumber())
                .accountStatus(dto.getAccountStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .lastLoginAt(dto.getLastLoginAt())
                .emailVerifiedAt(dto.getEmailVerifiedAt())
                .build();
    }

    private User toDomainUser(UserAuthInfoDto dto) {
        if (dto == null) return null;

        return User.builder()
                .id(new UserId(dto.getId()))
                .email(new Email(dto.getEmail()))
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .emailVerified(dto.isEmailVerified())
                .build();
    }

    private UserStatistics toDomainStatistics(UserStatisticsDto dto) {
        if (dto == null) return null;

        return UserStatistics.builder()
                .userId(dto.getUserId())
                .totalOrders(dto.getTotalOrders())
                .totalSpent(dto.getTotalSpent())
                .averageOrderValue(dto.getAverageOrderValue())
                .totalReviews(dto.getTotalReviews())
                .averageRating(dto.getAverageRating())
                .lastOrderDate(dto.getLastOrderDate())
                .preferredCategory(dto.getPreferredCategory())
                .build();
    }

    private Page<User> toDomainPage(UserPageDto dto) {
        if (dto == null) return Page.empty();

        return Page.<User>builder()
                .content(dto.getContent().stream().map(this::toDomainUser).toList())
                .pageNumber(dto.getPageNumber())
                .pageSize(dto.getPageSize())
                .totalElements(dto.getTotalElements())
                .totalPages(dto.getTotalPages())
                .last(dto.isLast())
                .first(dto.isFirst())
                .numberOfElements(dto.getNumberOfElements())
                .empty(dto.isEmpty())
                .build();
    }
}