package com.mygitgor.seller_service.domain.model;

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
    private Integer quantity;
    private String status;
}
