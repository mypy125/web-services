package com.mygitgor.user_service.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCouponStatsRequest {
    private String couponCode;
    private Double discountAmount;
}
