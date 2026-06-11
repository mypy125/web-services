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
public class ProductStatsUpdatedEvent {
    private String userId;
    private Integer totalProductsPurchased;
    private String mostPurchasedCategory;
    private String favoriteProductId;
    private String favoriteProductName;
    private String lastPurchasedProductId;
    private String lastPurchasedCategory;
    private LocalDateTime occurredAt;
}
