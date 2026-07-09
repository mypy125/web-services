package com.mygitgor.seller_service.infrastructure.kafka.event;

import java.time.LocalDateTime;

public record SellerDeletedEvent(
        String sellerId,
        String email,
        LocalDateTime timestamp
) {}
