package com.mygitgor.user_service.infrastructure.dto.external;

import lombok.Builder;

@Builder
public record CartItemSummaryDto(
        String productId,
        String productName,
        Integer quantity,
        Double price,
        Double totalPrice,
        String productImage
) {
    public double calculateTotalPrice() {
        return (price != null && quantity != null) ? price * quantity : 0.0;
    }
}
