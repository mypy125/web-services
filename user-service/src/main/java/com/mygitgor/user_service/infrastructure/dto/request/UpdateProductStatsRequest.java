package com.mygitgor.user_service.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductStatsRequest {
    private String productId;
    private String productName;
    private String category;
    private Integer quantity;
    private Double price;
}
