package com.mygitgor.auth_service.domain.cart.model;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItem {
    private final String productId;
    private final String productName;
    private final String productImage;
    private final String sellerId;
    private final String sellerName;
    private double price;
    private int quantity;
    private double totalPrice;
    private String variantId;
    private String variantName;
    private int maxQuantity;
    private boolean inStock;

    public void updateQuantity(int newQuantity) {
        if (newQuantity > maxQuantity) {
            throw new DomainException("Quantity exceeds maximum allowed: " + maxQuantity);
        }
        if (newQuantity <= 0) {
            throw new DomainException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        this.totalPrice = this.price * this.quantity;
    }

    public void updatePrice(double newPrice) {
        this.price = newPrice;
        this.totalPrice = this.price * this.quantity;
    }

    public boolean isAvailable() {
        return inStock && quantity <= maxQuantity;
    }
}
