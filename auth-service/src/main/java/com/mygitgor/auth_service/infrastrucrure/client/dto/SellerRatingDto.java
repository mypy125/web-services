package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerRatingDto {
    private String sellerId;
    private double averageRating;
    private long totalRatings;
    private Map<Integer, Long> ratingDistribution;
    private long positiveFeedback;
    private long negativeFeedback;
    private double responseRate;
    private double onTimeDeliveryRate;
}
