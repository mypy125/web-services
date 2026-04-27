package com.mygitgor.auth_service.domain.seller.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class SellerRating {
    private final String sellerId;
    private final double averageRating;
    private final long totalRatings;
    private final Map<Integer, Long> ratingDistribution;
    private final long positiveFeedback;
    private final long negativeFeedback;
    private final double responseRate;
    private final double onTimeDeliveryRate;
}
