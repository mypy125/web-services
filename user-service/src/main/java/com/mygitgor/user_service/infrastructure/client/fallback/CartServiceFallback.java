package com.mygitgor.user_service.infrastructure.client.fallback;

import com.mygitgor.user_service.infrastructure.dto.external.CartSummaryDto;
import com.mygitgor.user_service.infrastructure.dto.external.CartItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CartServiceFallback {

    public Mono<CartSummaryDto> getUserCartSummary(String userId) {
        log.warn("Fallback: Returning empty cart summary for user: {}", userId);
        return Mono.empty();
    }

    public Mono<Integer> getCartItemsCount(String userId) {
        log.warn("Fallback: Returning 0 cart items count for user: {}", userId);
        return Mono.just(0);
    }

    public Mono<Void> clearCart(String userId) {
        log.warn("Fallback: Could not clear cart for user: {}", userId);
        return Mono.empty();
    }

    public Mono<CartSummaryDto> addItemToCart(String userId, CartItemRequest itemRequest) {
        log.warn("Fallback: Could not add item to cart for user: {}", userId);
        return Mono.empty();
    }

    public Mono<CartSummaryDto> removeItemFromCart(String userId, String productId) {
        log.warn("Fallback: Could not remove item from cart for user: {}, product: {}", userId, productId);
        return Mono.empty();
    }

    public Mono<CartSummaryDto> updateItemQuantity(String userId, String productId, Integer quantity) {
        log.warn("Fallback: Could not update item quantity for user: {}, product: {}, quantity: {}", userId, productId, quantity);
        return Mono.empty();
    }
}
