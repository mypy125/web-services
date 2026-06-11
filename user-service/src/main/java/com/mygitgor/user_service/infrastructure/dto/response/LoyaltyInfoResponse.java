package com.mygitgor.user_service.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyInfoResponse {
    private String userId;
    private String email;
    private Integer loyaltyPoints;
    private String loyaltyTier;
    private Integer currentLevel;
    private Integer nextLevelPoints;
    private Integer pointsToNextLevel;
    private Double progressToNextLevel;
    private Double cashbackRate;
    private Double discountRate;
    private String benefits;
}
