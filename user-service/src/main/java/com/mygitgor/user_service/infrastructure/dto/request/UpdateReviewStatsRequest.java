package com.mygitgor.user_service.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewStatsRequest {
    private Integer rating;
    private String productId;
    private String reviewId;
}
