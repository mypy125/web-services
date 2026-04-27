package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartValidationResultDto {
    private boolean valid;
    private List<String> errors;
    private Map<String, String> itemErrors;
    private List<CartItemDto> unavailableItems;
    private List<CartItemDto> priceChangedItems;
    private double originalTotal;
    private double updatedTotal;
}
