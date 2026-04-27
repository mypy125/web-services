package com.mygitgor.auth_service.domain.cart.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CartSummary {
    private final String cartId;
    private final int totalItems;
    private final int uniqueProducts;
    private final double subtotal;
    private final double discount;
    private final double shippingCost;
    private final double tax;
    private final double total;
    private final String couponCode;
    private final double savings;
    private final List<CartItemSummary> itemSummaries;

    @Getter
    @Builder
    public static class CartItemSummary {
        private final String productId;
        private final String productName;
        private final int quantity;
        private final double price;
        private final double totalPrice;
        private final boolean inStock;
    }

    public double getSavings() {
        return discount;
    }
}
