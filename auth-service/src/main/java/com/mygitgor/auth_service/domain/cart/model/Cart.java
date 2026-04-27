package com.mygitgor.auth_service.domain.cart.model;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class Cart {
    private final String id;
    private final UserId userId;
    private List<CartItem> items;
    private double subtotal;
    private double discount;
    private double shippingCost;
    private double tax;
    private double total;
    private String couponCode;
    private double couponDiscount;
    private CartStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime expiresAt;

    @Builder.Default
    private int totalItems = 0;

    @Builder.Default
    private boolean itemsReserved = false;

    public void addItem(CartItem newItem) {
        if (items == null) {
            items = new ArrayList<>();
        }

        CartItem existingItem = items.stream()
                .filter(item -> item.getProductId().equals(newItem.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + newItem.getQuantity());
        } else {
            items.add(newItem);
        }

        recalculateTotals();
        updateTimestamp();
    }

    public void updateItemQuantity(String productId, int quantity) {
        CartItem item = items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Item not found in cart"));

        if (quantity <= 0) {
            items.remove(item);
        } else {
            item.updateQuantity(quantity);
        }

        recalculateTotals();
        updateTimestamp();
    }

    public void removeItem(String productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotals();
        updateTimestamp();
    }

    public void clear() {
        items.clear();
        recalculateTotals();
        updateTimestamp();
    }

    public void applyCoupon(String code, double discountAmount, double discountPercentage) {
        this.couponCode = code;
        this.couponDiscount = discountAmount > 0 ? discountAmount : (subtotal * discountPercentage / 100);
        this.discount = this.couponDiscount;
        recalculateTotals();
        updateTimestamp();
    }

    public void removeCoupon() {
        this.couponCode = null;
        this.couponDiscount = 0;
        this.discount = 0;
        recalculateTotals();
        updateTimestamp();
    }

    public void reserveItems() {
        if (itemsReserved) {
            throw new DomainException("Items already reserved");
        }
        this.itemsReserved = true;
        this.status = CartStatus.RESERVED;
        updateTimestamp();
    }

    public void releaseItems() {
        this.itemsReserved = false;
        this.status = CartStatus.ACTIVE;
        updateTimestamp();
    }

    public void markAsAbandoned() {
        this.status = CartStatus.ABANDONED;
        updateTimestamp();
    }

    private void recalculateTotals() {
        if (items == null) {
            items = new ArrayList<>();
        }

        this.subtotal = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        this.totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        this.total = subtotal - discount + shippingCost + tax;
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void updateShippingCost(double cost) {
        this.shippingCost = cost;
        recalculateTotals();
    }

    public void updateTax(double taxAmount) {
        this.tax = taxAmount;
        recalculateTotals();
    }

    public static Cart create(UserId userId) {
        LocalDateTime now = LocalDateTime.now();
        return Cart.builder()
                .id(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .items(new ArrayList<>())
                .subtotal(0.0)
                .discount(0.0)
                .shippingCost(0.0)
                .tax(0.0)
                .total(0.0)
                .status(CartStatus.ACTIVE)
                .totalItems(0)
                .itemsReserved(false)
                .createdAt(now)
                .updatedAt(now)
                .lastActivityAt(now)
                .build();
    }
}
