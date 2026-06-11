package com.mygitgor.user_service.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsUpdatedEvent {
    private String userId;
    private Integer totalReviews;
    private Double averageRating;
    private LocalDateTime occurredAt;
}
