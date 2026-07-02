package com.mygitgor.user_service.application.dto.external;

import lombok.Builder;
import java.util.List;

@Builder
public record CartSummaryDto(
        String id,
        String userId,
        Integer totalItems,
        Double subtotal,
        Double discount,
        Double shippingCost,
        Double tax,
        Double total,
        String couponCode,
        List<CartItemSummaryDto> items
) {
    public CartSummaryDto {
        if (items == null) {
            items = List.of();
        }
    }

    public boolean hasCoupon() {
        return couponCode != null && !couponCode.isBlank();
    }
}
