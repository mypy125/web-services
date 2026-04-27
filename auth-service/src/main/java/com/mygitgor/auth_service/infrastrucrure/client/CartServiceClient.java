package com.mygitgor.auth_service.infrastrucrure.client;

import com.mygitgor.auth_service.domain.cart.model.Cart;
import com.mygitgor.auth_service.domain.cart.model.CartAnalytics;
import com.mygitgor.auth_service.domain.cart.model.CartItem;
import com.mygitgor.auth_service.domain.cart.model.CartSummary;
import com.mygitgor.auth_service.domain.cart.model.CartValidationResult;
import com.mygitgor.auth_service.domain.cart.port.CartPort;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.client.dto.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import com.mygitgor.auth_service.infrastrucrure.client.exception.CartNotFoundException;
import com.mygitgor.auth_service.infrastrucrure.client.exception.ServiceClientException;
import com.mygitgor.auth_service.infrastrucrure.client.fallback.CartServiceFallback;
import com.mygitgor.auth_service.infrastrucrure.client.interceptor.ServiceClientInterceptor;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;
import java.time.Duration;
import java.util.stream.Collectors;


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
                    if (response.statusCode().value() == 404) {
                        return new CartNotFoundException("Cart not found: " + identifier);
                    }
                    if (response.statusCode().value() == 400) {
                        return new IllegalArgumentException("Bad request: " + errorBody);
                    }
                    return new ServiceClientException("Client error during " + operation + ": " + errorBody);
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Cart service unavailable during " + operation));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "createCartFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Cart> createCart(UserId userId) {
        log.info("Creating cart for user: {}", userId);

        CreateCartRequestDto request = CreateCartRequestDto.builder()
                .userId(userId.toString())
                .build();

        return webClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "create cart", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "create cart", userId.toString()))
                .bodyToMono(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(cart -> log.info("Cart created successfully for user: {}", userId))
                .doOnError(error -> log.error("Failed to create cart for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "getCartByUserIdFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Cart> getCartByUserId(UserId userId) {
        log.debug("Fetching cart for user: {}", userId);

        return webClient.get()
                .uri("/user/{userId}", userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::NOT_FOUND::equals, response -> {
            log.debug("Cart not found for user: {}, returning empty cart", userId);
            return Mono.just(Cart.create(userId));
        })
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "get cart", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "get cart", userId.toString()))
                .bodyToMono(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(cart -> log.debug("Cart fetched successfully for user: {}", userId))
                .doOnError(error -> log.error("Failed to fetch cart for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "getCartByIdFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Cart> getCartById(String cartId) {
        log.debug("Fetching cart by ID: {}", cartId);

        return webClient.get()
                .uri("/{cartId}", cartId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "get cart by id", cartId))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "get cart by id", cartId))
                .bodyToMono(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "addItemToCartFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Cart> addItemToCart(UserId userId, CartItem cartItem) {
        log.info("Adding item to cart for user: {} - Product: {}", userId, cartItem.getProductId());

        AddItemRequestDto request = AddItemRequestDto.builder()
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .variantId(cartItem.getVariantId())
                .build();

        return webClient.post()
                .uri("/user/{userId}/items", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "add item to cart", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "add item to cart", userId.toString()))
                .bodyToMono(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(cart -> log.info("Item added to cart for user: {}", userId))
                .doOnError(error -> log.error("Failed to add item to cart for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "updateCartItemQuantityFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Cart> updateCartItemQuantity(UserId userId, String productId, int quantity) {
        log.info("Updating item quantity for user: {} - Product: {}, Quantity: {}", userId, productId, quantity);

        UpdateQuantityRequestDto request = UpdateQuantityRequestDto.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        return webClient.put()
                .uri("/user/{userId}/items/{productId}", userId.toString(), productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "update item quantity", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "update item quantity", userId.toString()))
                .bodyToMono(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(cart -> log.info("Item quantity updated for user: {}", userId))
                .doOnError(error -> log.error("Failed to update item quantity for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "removeItemFromCartFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Cart> removeItemFromCart(UserId userId, String productId) {
        log.info("Removing item from cart for user: {} - Product: {}", userId, productId);

        return webClient.delete()
                .uri("/user/{userId}/items/{productId}", userId.toString(), productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "remove item from cart", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "remove item from cart", userId.toString()))
                .bodyToMono(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(cart -> log.info("Item removed from cart for user: {}", userId))
                .doOnError(error -> log.error("Failed to remove item from cart for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "clearCartFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Void> clearCart(UserId userId) {
        log.info("Clearing cart for user: {}", userId);

        return webClient.delete()
                .uri("/user/{userId}", userId.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "clear cart", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "clear cart", userId.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .then()
                .doOnSuccess(v -> log.info("Cart cleared for user: {}", userId))
                .doOnError(error -> log.warn("Failed to clear cart for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "getCartSummaryFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<CartSummary> getCartSummary(UserId userId) {
        log.debug("Getting cart summary for user: {}", userId);

        return webClient.get()
                .uri("/user/{userId}/summary", userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "get cart summary", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "get cart summary", userId.toString()))
                .bodyToMono(CartSummaryDto.class)
                .map(this::toDomainCartSummary)
                .timeout(Duration.ofMillis(timeout))
                .doOnError(error -> log.warn("Failed to get cart summary for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "applyCouponFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Cart> applyCoupon(UserId userId, String couponCode) {
        log.info("Applying coupon to cart for user: {} - Coupon: {}", userId, couponCode);

        ApplyCouponRequestDto request = ApplyCouponRequestDto.builder()
                .couponCode(couponCode)
                .build();

        return webClient.post()
                .uri("/user/{userId}/coupon", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "apply coupon", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "apply coupon", userId.toString()))
                .bodyToMono(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(cart -> log.info("Coupon applied to cart for user: {}", userId))
                .doOnError(error -> log.error("Failed to apply coupon for user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "cartService", fallbackMethod = "removeCouponFallback")
    @Retry(name = "cartService")
    @TimeLimiter(name = "cartService")
    public Mono<Cart> removeCoupon(UserId userId) {
        log.info("Removing coupon from cart for user: {}", userId);

        return webClient.delete()
                .uri("/user/{userId}/coupon", userId.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "remove coupon", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "remove coupon", userId.toString()))
                .bodyToMono(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(cart -> log.info("Coupon removed from cart for user: {}", userId));
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Mono<Integer> getCartItemCount(UserId userId) {
        log.debug("Getting cart item count for user: {}", userId);

        return webClient.get()
                .uri("/user/{userId}/count", userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "get cart item count", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "get cart item count", userId.toString()))
                .bodyToMono(Integer.class)
                .timeout(Duration.ofMillis(timeout))
                .defaultIfEmpty(0);
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Mono<Boolean> cartContainsProduct(UserId userId, String productId) {
        log.debug("Checking if cart contains product for user: {} - Product: {}", userId, productId);

        return webClient.get()
                .uri("/user/{userId}/contains/{productId}", userId.toString(), productId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "check product in cart", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "check product in cart", userId.toString()))
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofMillis(timeout))
                .defaultIfEmpty(false);
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Mono<Cart> mergeCarts(UserId userId, String guestCartId) {
        log.info("Merging guest cart {} with user cart {}", guestCartId, userId);

        MergeCartsRequestDto request = MergeCartsRequestDto.builder()
                .guestCartId(guestCartId)
                .userId(userId.toString())
                .build();

        return webClient.post()
                .uri("/merge")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "merge carts", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "merge carts", userId.toString()))
                .bodyToMono(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(cart -> log.info("Carts merged successfully for user: {}", userId));
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Mono<CartValidationResult> validateCartForCheckout(UserId userId) {
        log.info("Validating cart for checkout - User: {}", userId);

        return webClient.get()
                .uri("/user/{userId}/validate", userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "validate cart", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "validate cart", userId.toString()))
                .bodyToMono(CartValidationResultDto.class)
                .map(this::toDomainValidationResult)
                .timeout(Duration.ofMillis(timeout));
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Mono<Boolean> reserveCartItems(UserId userId) {
        log.info("Reserving cart items for user: {}", userId);

        return webClient.post()
                .uri("/user/{userId}/reserve", userId.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "reserve cart items", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "reserve cart items", userId.toString()))
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofMillis(timeout))
                .defaultIfEmpty(false)
                .doOnSuccess(reserved -> log.info("Cart items reserved for user {}: {}", userId, reserved));
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Mono<Void> releaseReservedItems(UserId userId) {
        log.info("Releasing reserved cart items for user: {}", userId);

        return webClient.post()
                .uri("/user/{userId}/release", userId.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "release reserved items", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "release reserved items", userId.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .then()
                .doOnSuccess(v -> log.info("Reserved items released for user: {}", userId));
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Mono<Void> deleteCart(UserId userId) {
        log.info("Deleting cart for user: {}", userId);

        return webClient.delete()
                .uri("/user/{userId}/permanent", userId.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> handleClientErrorResponse(response, "delete cart", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerErrorResponse(response, "delete cart", userId.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .then()
                .doOnSuccess(v -> log.info("Cart deleted for user: {}", userId));
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Flux<Cart> getActiveCarts(int page, int size) {
        log.debug("Getting active carts - page: {}, size: {}", page, size);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/active")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ServiceClientException("Client error getting active carts")))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ServiceUnavailableException("Cart service unavailable")))
                .bodyToFlux(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout));
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Flux<Cart> getAbandonedCarts(int daysThreshold) {
        log.debug("Getting abandoned carts - threshold: {} days", daysThreshold);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/abandoned")
                        .queryParam("days", daysThreshold)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ServiceClientException("Client error getting abandoned carts")))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ServiceUnavailableException("Cart service unavailable")))
                .bodyToFlux(CartDto.class)
                .map(this::toDomainCart)
                .timeout(Duration.ofMillis(timeout));
    }

    @Override
    @CircuitBreaker(name = "cartService")
    @Retry(name = "cartService")
    public Mono<CartAnalytics> getCartAnalytics() {
        log.debug("Getting cart analytics");

        return webClient.get()
                .uri("/analytics")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ServiceClientException("Client error getting cart analytics")))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ServiceUnavailableException("Cart service unavailable")))
                .bodyToMono(CartAnalyticsDto.class)
                .map(this::toDomainAnalytics)
                .timeout(Duration.ofMillis(timeout));
    }

    // Fallback methods
    public Mono<Cart> createCartFallback(UserId userId, Throwable t) {
        log.warn("Fallback: createCart for user {} due to: {}", userId, t.getMessage());
        return fallback.createCart(userId);
    }

    public Mono<Cart> getCartByUserIdFallback(UserId userId, Throwable t) {
        log.warn("Fallback: getCartByUserId for user {} due to: {}", userId, t.getMessage());
        return fallback.getCartByUserId(userId);
    }

    public Mono<Cart> getCartByIdFallback(String cartId, Throwable t) {
        log.warn("Fallback: getCartById for {} due to: {}", cartId, t.getMessage());
        return fallback.getCartById(cartId);
    }

    public Mono<Cart> addItemToCartFallback(UserId userId, CartItem cartItem, Throwable t) {
        log.warn("Fallback: addItemToCart for user {} due to: {}", userId, t.getMessage());
        return fallback.addItemToCart(userId, cartItem);
    }

    public Mono<Cart> updateCartItemQuantityFallback(UserId userId, String productId, int quantity, Throwable t) {
        log.warn("Fallback: updateCartItemQuantity for user {} due to: {}", userId, t.getMessage());
        return fallback.updateCartItemQuantity(userId, productId, quantity);
    }

    public Mono<Cart> removeItemFromCartFallback(UserId userId, String productId, Throwable t) {
        log.warn("Fallback: removeItemFromCart for user {} due to: {}", userId, t.getMessage());
        return fallback.removeItemFromCart(userId, productId);
    }

    public Mono<Void> clearCartFallback(UserId userId, Throwable t) {
        log.warn("Fallback: clearCart for user {} due to: {}", userId, t.getMessage());
        return fallback.clearCart(userId);
    }

    public Mono<CartSummary> getCartSummaryFallback(UserId userId, Throwable t) {
        log.warn("Fallback: getCartSummary for user {} due to: {}", userId, t.getMessage());
        return fallback.getCartSummary(userId);
    }

    public Mono<Cart> applyCouponFallback(UserId userId, String couponCode, Throwable t) {
        log.warn("Fallback: applyCoupon for user {} due to: {}", userId, t.getMessage());
        return fallback.applyCoupon(userId, couponCode);
    }

    public Mono<Cart> removeCouponFallback(UserId userId, Throwable t) {
        log.warn("Fallback: removeCoupon for user {} due to: {}", userId, t.getMessage());
        return fallback.removeCoupon(userId);
    }

    // Mappers
    private Cart toDomainCart(CartDto dto) {
        if (dto == null) return null;

        return Cart.builder()
                .id(dto.getId())
                .userId(new UserId(dto.getUserId()))
                .items(dto.getItems() != null ? dto.getItems().stream()
                        .map(this::toDomainCartItem)
                        .collect(Collectors.toList()) : null)
                .subtotal(dto.getSubtotal())
                .discount(dto.getDiscount())
                .shippingCost(dto.getShippingCost())
                .tax(dto.getTax())
                .total(dto.getTotal())
                .couponCode(dto.getCouponCode())
                .couponDiscount(dto.getCouponDiscount())
                .totalItems(dto.getTotalItems())
                .itemsReserved(dto.isItemsReserved())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .lastActivityAt(dto.getLastActivityAt())
                .build();
    }

    private CartItem toDomainCartItem(CartItemDto dto) {
        if (dto == null) return null;

        return CartItem.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .productImage(dto.getProductImage())
                .sellerId(dto.getSellerId())
                .sellerName(dto.getSellerName())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .totalPrice(dto.getTotalPrice())
                .variantId(dto.getVariantId())
                .variantName(dto.getVariantName())
                .maxQuantity(dto.getMaxQuantity())
                .inStock(dto.isInStock())
                .build();
    }

    private CartSummary toDomainCartSummary(CartSummaryDto dto) {
        if (dto == null) return null;

        return CartSummary.builder()
                .cartId(dto.getCartId())
                .totalItems(dto.getTotalItems())
                .uniqueProducts(dto.getUniqueProducts())
                .subtotal(dto.getSubtotal())
                .discount(dto.getDiscount())
                .shippingCost(dto.getShippingCost())
                .tax(dto.getTax())
                .total(dto.getTotal())
                .couponCode(dto.getCouponCode())
                .itemSummaries(dto.getItemSummaries() != null ?
                        dto.getItemSummaries().stream()
                                .map(this::toDomainItemSummary)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    private CartSummary.CartItemSummary toDomainItemSummary(CartItemSummaryDto dto) {
        return CartSummary.CartItemSummary.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .totalPrice(dto.getTotalPrice())
                .inStock(dto.isInStock())
                .build();
    }

    private CartValidationResult toDomainValidationResult(CartValidationResultDto dto) {
        if (dto == null) return null;

        return CartValidationResult.builder()
                .valid(dto.isValid())
                .errors(dto.getErrors())
                .itemErrors(dto.getItemErrors())
                .unavailableItems(dto.getUnavailableItems() != null ?
                        dto.getUnavailableItems().stream()
                                .map(this::toDomainCartItem)
                                .collect(Collectors.toList()) : null)
                .priceChangedItems(dto.getPriceChangedItems() != null ?
                        dto.getPriceChangedItems().stream()
                                .map(this::toDomainCartItem)
                                .collect(Collectors.toList()) : null)
                .originalTotal(dto.getOriginalTotal())
                .updatedTotal(dto.getUpdatedTotal())
                .build();
    }

    private CartAnalytics toDomainAnalytics(CartAnalyticsDto dto) {
        if (dto == null) return null;

        return CartAnalytics.builder()
                .totalActiveCarts(dto.getTotalActiveCarts())
                .totalAbandonedCarts(dto.getTotalAbandonedCarts())
                .averageCartValue(dto.getAverageCartValue())
                .conversionRate(dto.getConversionRate())
                .topProductsInCarts(dto.getTopProductsInCarts())
                .cartsByHour(dto.getCartsByHour())
                .averageItemsPerCart(dto.getAverageItemsPerCart())
                .cartsWithCoupons(dto.getCartsWithCoupons())
                .build();
    }
}
