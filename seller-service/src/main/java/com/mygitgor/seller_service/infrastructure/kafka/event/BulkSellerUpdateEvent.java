package com.mygitgor.seller_service.infrastructure.kafka.event;

import com.mygitgor.seller_service.domain.model.Seller;

import java.time.LocalDateTime;
import java.util.List;

public record BulkSellerUpdateEvent(
        String action,
        List<Seller> sellers,
        LocalDateTime timestamp
) {}