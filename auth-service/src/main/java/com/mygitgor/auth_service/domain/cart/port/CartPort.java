package com.mygitgor.auth_service.domain.cart.port;

import com.mygitgor.auth_service.domain.cart.model.*;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface CartPort {
    Mono<Cart> createCart(UserId userId);
    Mono<Cart> getCartByUserId(UserId userId);
    Mono<Cart> getCartById(String cartId);
    Mono<Cart> addItemToCart(UserId userId, CartItem cartItem);
    Mono<Cart> updateCartItemQuantity(UserId userId, String productId, int quantity);
    Mono<Cart> removeItemFromCart(UserId userId, String productId);
    Mono<Void> clearCart(UserId userId);
    Mono<CartSummary> getCartSummary(UserId userId);
    Mono<Cart> applyCoupon(UserId userId, String couponCode);
    Mono<Cart> removeCoupon(UserId userId);
    Mono<Integer> getCartItemCount(UserId userId);
    Mono<Boolean> cartContainsProduct(UserId userId, String productId);
    Mono<Cart> mergeCarts(UserId userId, String guestCartId);
    Mono<CartValidationResult> validateCartForCheckout(UserId userId);
    Mono<Boolean> reserveCartItems(UserId userId);
    Mono<Void> releaseReservedItems(UserId userId);
    Mono<Void> deleteCart(UserId userId);
    Flux<Cart> getActiveCarts(int page, int size);
    Flux<Cart> getAbandonedCarts(int daysThreshold);
    Mono<CartAnalytics> getCartAnalytics();
}
