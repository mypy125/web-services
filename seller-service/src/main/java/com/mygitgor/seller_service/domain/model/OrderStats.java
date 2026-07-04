package com.mygitgor.seller_service.domain.model;

import lombok.Builder;

@Builder
public record OrderStats (
        Double amount,
        Double tax,
        Double shippingCost,
        Double discount,
        Double commission,
        String status,
        boolean isNewCustomer,
        String productId,
        String productName,
        String category,
        Integer quantity,
        Double productPrice,
        Double productTotal,
        String customerId,
        boolean isFirstPurchase
){
    @Override
    public Double productTotal() {
        if (productTotal != null) {
            return productTotal;
        }
        if (productPrice != null && quantity != null) {
            return productPrice * quantity;
        }
        return amount != null ? amount : 0.0;
    }
}
