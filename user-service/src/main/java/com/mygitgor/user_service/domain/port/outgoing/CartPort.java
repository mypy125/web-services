package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.application.dto.external.CartItemRequest;
import com.mygitgor.user_service.application.dto.external.CartSummaryDto;
import reactor.core.publisher.Mono;

public interface CartPort {
    Mono<CartSummaryDto> getUserCartSummary(String userId);
    Mono<Integer> getCartItemsCount(String userId);
    Mono<Void> clearCart(String userId);
    Mono<CartSummaryDto> addItemToCart(String userId, CartItemRequest itemRequest);
    Mono<CartSummaryDto> removeItemFromCart(String userId, String productId);
    Mono<CartSummaryDto> updateItemQuantity(String userId, String productId, Integer quantity);
}
