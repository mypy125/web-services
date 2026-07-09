package com.mygitgor.seller_service.infrastructure.kafka.event;

import com.mygitgor.seller_service.shared.valueobject.Address;

import java.time.LocalDateTime;

public record SellerAddressEvent(
        String sellerId,
        Address address,
        String addressType,
        LocalDateTime timestamp
) {}
