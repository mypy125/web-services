package com.mygitgor.auth_service.domain.cart.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class CartValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final Map<String, String> itemErrors;
    private final List<CartItem> unavailableItems;
    private final List<CartItem> priceChangedItems;
    private final double originalTotal;
    private final double updatedTotal;

    public boolean hasUnavailableItems() {
        return unavailableItems != null && !unavailableItems.isEmpty();
    }

    public boolean hasPriceChanges() {
        return priceChangedItems != null && !priceChangedItems.isEmpty();
    }

    public String getErrorMessage() {
        if (valid) return null;
        return errors != null && !errors.isEmpty() ? String.join("; ", errors) : "Cart validation failed";
    }
}
