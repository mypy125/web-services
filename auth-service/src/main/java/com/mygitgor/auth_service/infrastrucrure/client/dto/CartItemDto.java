package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private String productId;
    private String productName;
    private String productImage;
    private String sellerId;
    private String sellerName;
    private double price;
    private int quantity;
    private double totalPrice;
    private String variantId;
    private String variantName;
    private int maxQuantity;
    private boolean inStock;
}
