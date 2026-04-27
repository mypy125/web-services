package com.mygitgor.auth_service.domain.user.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserStatistics {
    private final String userId;
    private final long totalOrders;
    private final double totalSpent;
    private final double averageOrderValue;
    private final int totalReviews;
    private final double averageRating;
    private final LocalDateTime lastOrderDate;
    private final String preferredCategory;
}
