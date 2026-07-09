package com.mygitgor.seller_service.infrastructure.kafka.event;

import java.time.LocalDateTime;

public record SellerProductDeletedEvent(
        String productId,
        String sellerId,
        LocalDateTime timestamp
) {}
