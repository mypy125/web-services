package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemSummaryDto {
    private String productId;
    private String productName;
    private int quantity;
    private double price;
    private double totalPrice;
    private boolean inStock;
}
