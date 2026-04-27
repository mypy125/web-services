package com.mygitgor.auth_service.infrastrucrure.client.fallback;

import com.mygitgor.auth_service.domain.cart.model.Cart;
import com.mygitgor.auth_service.domain.cart.model.CartItem;
import com.mygitgor.auth_service.domain.cart.model.CartSummary;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.client.dto.CartDto;
import com.mygitgor.auth_service.infrastrucrure.client.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CartServiceFallback {

    public Mono<Cart> createCart(UserId userId) {
        log.warn("CartService fallback: createCart for user {}", userId);
        Cart fallbackCart = Cart.create(userId);
        return Mono.just(fallbackCart);
    }

    public Mono<Cart> getCartByUserId(UserId userId) {
        log.warn("CartService fallback: getCartByUserId for user {}", userId);
        Cart fallbackCart = Cart.create(userId);
        return Mono.just(fallbackCart);
    }

    public Mono<Cart> getCartById(String cartId) {
        log.warn("CartService fallback: getCartById for {}", cartId);
        return Mono.error(new ServiceUnavailableException("Cart service is temporarily unavailable"));
    }

    public Mono<Cart> addItemToCart(UserId userId, CartItem cartItem) {
        log.warn("CartService fallback: addItemToCart for user {}", userId);
        Cart fallbackCart = Cart.create(userId);
        fallbackCart.addItem(cartItem);
        return Mono.just(fallbackCart);
    }

    public Mono<Cart> updateCartItemQuantity(UserId userId, String productId, int quantity) {
        log.warn("CartService fallback: updateCartItemQuantity for user {}", userId);
        Cart fallbackCart = Cart.create(userId);
        return Mono.just(fallbackCart);
    }

    public Mono<Cart> removeItemFromCart(UserId userId, String productId) {
        log.warn("CartService fallback: removeItemFromCart for user {}", userId);
        Cart fallbackCart = Cart.create(userId);
        return Mono.just(fallbackCart);
    }

    public Mono<Void> clearCart(UserId userId) {
        log.warn("CartService fallback: clearCart for user {}", userId);
        return Mono.empty();
    }

    public Mono<CartSummary> getCartSummary(UserId userId) {
        log.warn("CartService fallback: getCartSummary for user {}", userId);
        return Mono.just(CartSummary.builder()
                .cartId("fallback")
                .totalItems(0)
                .uniqueProducts(0)
                .subtotal(0.0)
                .discount(0.0)
                .shippingCost(0.0)
                .tax(0.0)
                .total(0.0)
                .build());
    }

    public Mono<Cart> applyCoupon(UserId userId, String couponCode) {
        log.warn("CartService fallback: applyCoupon for user {}", userId);
        return Mono.error(new ServiceUnavailableException("Coupon service unavailable"));
    }

    public Mono<Cart> removeCoupon(UserId userId) {
        log.warn("CartService fallback: removeCoupon for user {}", userId);
        Cart fallbackCart = Cart.create(userId);
        return Mono.just(fallbackCart);
    }
}