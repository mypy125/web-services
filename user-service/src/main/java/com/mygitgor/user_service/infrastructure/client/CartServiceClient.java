package com.mygitgor.user_service.infrastructure.client;

import com.mygitgor.user_service.domain.port.outgoing.CartPort;
import com.mygitgor.user_service.infrastructure.client.exception.CartServiceException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.user_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.user_service.infrastructure.client.fallback.CartServiceFallback;
import com.mygitgor.user_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import com.mygitgor.user_service.application.dto.external.CartItemRequest;
import com.mygitgor.user_service.application.dto.external.CartSummaryDto;
import com.mygitgor.user_service.application.dto.external.QuantityRequest;
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
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartServiceClient implements CartPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final CartServiceFallback fallback;

    @Value("${cart.service.url:http://localhost:8084/api/carts}")
    private String baseUrl;

    @Value("${cart.service.timeout:5000}")
    private int timeout;

    @Value("${cart.service.retry.attempts:3}")
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
                        case 404 -> CartServiceException.cartNotFound(identifier);
                        case 400 -> CartServiceException.invalidRequest(identifier, errorBody);
                        case 403 -> CartServiceException.accessDenied(identifier);
                        case 409 -> CartServiceException.conflict(identifier, errorBody);
                        default -> new CartServiceException(operation, statusCode,
                                "Client error: " + errorBody);
                    };
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Cart Service", operation));
    }

    private Mono<Throwable> handleTimeoutError(String operation, String identifier, Throwable cause) {
        log.error("Timeout during {} for {}: {}", operation, identifier, cause.getMessage());
        return Mono.just(new ServiceTimeoutException("Cart Service", operation, timeout));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "getUserCartSummaryFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<CartSummaryDto> getUserCartSummary(String userId) {
        log.debug("Fetching cart summary for user: {}", userId);

        return webClient.get()
                .uri("/users/{userId}/summary", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_CART_SUMMARY", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_CART_SUMMARY", userId))
                .bodyToMono(CartSummaryDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Cart Service", "GET_CART_SUMMARY", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(cart -> {
                    if (cart != null) {
                        log.debug("Cart summary fetched for user: {}, items: {}", userId, cart.totalItems());
                    } else {
                        log.debug("No cart found for user: {}", userId);
                    }
                })
                .doOnError(error -> log.error("Failed to fetch cart summary for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "getCartItemsCountFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Integer> getCartItemsCount(String userId) {
        log.debug("Fetching cart items count for user: {}", userId);

        return webClient.get()
                .uri("/users/{userId}/items/count", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "GET_CART_ITEMS_COUNT", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "GET_CART_ITEMS_COUNT", userId))
                .bodyToMono(Integer.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Cart Service", "GET_CART_ITEMS_COUNT", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .defaultIfEmpty(0)
                .doOnSuccess(count -> log.debug("Cart items count for user {}: {}", userId, count))
                .doOnError(error -> log.error("Failed to fetch cart items count for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "clearCartFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Void> clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);

        return webClient.delete()
                .uri("/users/{userId}/cart", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "CLEAR_CART", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "CLEAR_CART", userId))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Cart Service", "CLEAR_CART", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .then()
                .doOnSuccess(v -> log.info("Cart cleared for user: {}", userId))
                .doOnError(error -> log.error("Failed to clear cart for user {}: {}", userId, error.getMessage()));
    }


    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "addItemToCartFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<CartSummaryDto> addItemToCart(String userId, CartItemRequest itemRequest) {
        log.info("Adding item to cart for user: {}", userId);

        return webClient.post()
                .uri("/users/{userId}/items", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(itemRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "ADD_ITEM_TO_CART", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "ADD_ITEM_TO_CART", userId))
                .bodyToMono(CartSummaryDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Cart Service", "ADD_ITEM_TO_CART", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(cart -> log.info("Item added to cart for user: {}", userId))
                .doOnError(error -> log.error("Failed to add item to cart for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "removeItemFromCartFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<CartSummaryDto> removeItemFromCart(String userId, String productId) {
        log.info("Removing item from cart for user: {}, product: {}", userId, productId);

        return webClient.delete()
                .uri("/users/{userId}/items/{productId}", userId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "REMOVE_ITEM_FROM_CART", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "REMOVE_ITEM_FROM_CART", userId))
                .bodyToMono(CartSummaryDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Cart Service", "REMOVE_ITEM_FROM_CART", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(cart -> log.info("Item removed from cart for user: {}", userId))
                .doOnError(error -> log.error("Failed to remove item from cart for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "updateItemQuantityFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<CartSummaryDto> updateItemQuantity(String userId, String productId, Integer quantity) {
        log.info("Updating item quantity in cart for user: {}, product: {}, quantity: {}", userId, productId, quantity);

        return webClient.patch()
                .uri("/users/{userId}/items/{productId}", userId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new QuantityRequest(quantity))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "UPDATE_ITEM_QUANTITY", userId))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "UPDATE_ITEM_QUANTITY", userId))
                .bodyToMono(CartSummaryDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return Mono.error(new ServiceTimeoutException("Cart Service", "UPDATE_ITEM_QUANTITY", timeout));
                    }
                    return Mono.error(e);
                })
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(cart -> log.info("Item quantity updated in cart for user: {}", userId))
                .doOnError(error -> log.error("Failed to update item quantity in cart for user {}: {}", userId, error.getMessage()));
    }


    private Mono<CartSummaryDto> getUserCartSummaryFallback(String userId, Throwable t) {
        log.warn("Fallback: getUserCartSummary for user {} due to: {}", userId, t.getMessage());
        return fallback.getUserCartSummary(userId);
    }

    private Mono<Integer> getCartItemsCountFallback(String userId, Throwable t) {
        log.warn("Fallback: getCartItemsCount for user {} due to: {}", userId, t.getMessage());
        return fallback.getCartItemsCount(userId);
    }

    private Mono<Void> clearCartFallback(String userId, Throwable t) {
        log.warn("Fallback: clearCart for user {} due to: {}", userId, t.getMessage());
        return fallback.clearCart(userId);
    }

    private Mono<CartSummaryDto> addItemToCartFallback(String userId, CartItemRequest itemRequest, Throwable t) {
        log.warn("Fallback: addItemToCart for user {} due to: {}", userId, t.getMessage());
        return fallback.addItemToCart(userId, itemRequest);
    }

    private Mono<CartSummaryDto> removeItemFromCartFallback(String userId, String productId, Throwable t) {
        log.warn("Fallback: removeItemFromCart for user {} due to: {}", userId, t.getMessage());
        return fallback.removeItemFromCart(userId, productId);
    }

    private Mono<CartSummaryDto> updateItemQuantityFallback(String userId, String productId, Integer quantity, Throwable t) {
        log.warn("Fallback: updateItemQuantity for user {} due to: {}", userId, t.getMessage());
        return fallback.updateItemQuantity(userId, productId, quantity);
    }
}
