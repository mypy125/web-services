package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryDto {
    private String cartId;
    private int totalItems;
    private int uniqueProducts;
    private double subtotal;
    private double discount;
    private double shippingCost;
    private double tax;
    private double total;
    private String couponCode;
    private List<CartItemSummaryDto> itemSummaries;
}
