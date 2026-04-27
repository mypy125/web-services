package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDto {
    private String userId;
    private long totalOrders;
    private double totalSpent;
    private double averageOrderValue;
    private int totalReviews;
    private double averageRating;
    private LocalDateTime lastOrderDate;
    private String preferredCategory;
}
