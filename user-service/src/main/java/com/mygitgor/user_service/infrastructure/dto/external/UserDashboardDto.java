package com.mygitgor.user_service.infrastructure.dto.external;

import java.util.List;
import lombok.Builder;

@Builder
public record UserDashboardDto(
        UserProfileDto profile,
        Integer cartItemsCount,
        List<OrderSummaryDto> recentOrders,
        Integer totalOrders,
        Double totalSpent
) {
    public UserDashboardDto {
        if (recentOrders == null) {
            recentOrders = List.of();
        }
    }
}
