package com.mygitgor.user_service.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatsRequest {
    private Double orderAmount;
    private String orderId;
    private LocalDateTime orderDate;
}
