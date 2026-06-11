package com.mygitgor.user_service.infrastructure.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDto {
    private String id;
    private String orderNumber;
    private Double totalAmount;
    private String status;
    private Integer itemsCount;
    private LocalDateTime createdAt;
}
