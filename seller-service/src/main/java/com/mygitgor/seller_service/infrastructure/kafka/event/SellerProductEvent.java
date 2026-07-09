package com.mygitgor.seller_service.infrastructure.kafka.event;

import com.mygitgor.seller_service.application.dto.response.ProductResponse;

import java.time.LocalDateTime;

public record SellerProductEvent(
        String productId,
        String sellerId,
        ProductResponse product,
        LocalDateTime timestamp
) {}
