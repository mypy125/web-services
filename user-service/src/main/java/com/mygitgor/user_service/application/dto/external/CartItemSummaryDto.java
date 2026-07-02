package com.mygitgor.user_service.application.dto.external;

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
