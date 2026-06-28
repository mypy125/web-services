package com.mygitgor.seller_service.shared.valueobject;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderStats {
    private Double amount;
    private Double tax;
    private Double shippingCost;
    private Double discount;
    private Double commission;
    private String status;
    private boolean isNewCustomer;
    private String productId;
    private String productName;
    private String category;
    private Integer quantity;
    private Double productPrice;
    private Double productTotal;
    private String customerId;
    private boolean isFirstPurchase;

    public Double getProductTotal() {
        if (productTotal != null) {
            return productTotal;
        }
        if (productPrice != null && quantity != null) {
            return productPrice * quantity;
        }
        return amount != null ? amount : 0.0;
    }
}
