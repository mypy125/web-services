package com.mygitgor.seller_service.infrastructure.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Event emitted when a seller is activated")
public record SellerActivatedEvent(

        @Schema(description = "Seller ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String sellerId,

        @Schema(description = "Seller email", example = "seller@example.com")
        String email,

        @Schema(description = "Seller name", example = "Tech Store")
        String sellerName,

        @Schema(description = "Store name", example = "Tech Store")
        String storeName,

        @Schema(description = "Activated by", example = "admin@example.com")
        String activatedBy,

        @Schema(description = "Activated at")
        LocalDateTime activatedAt,

        @Schema(description = "Event timestamp")
        LocalDateTime occurredAt
) {}
