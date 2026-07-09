package com.mygitgor.seller_service.infrastructure.kafka.event;

import com.mygitgor.seller_service.domain.model.Seller;

import java.time.LocalDateTime;

public record SellerEvent(
        String sellerId,
        Seller seller,
        LocalDateTime timestamp
) {}
