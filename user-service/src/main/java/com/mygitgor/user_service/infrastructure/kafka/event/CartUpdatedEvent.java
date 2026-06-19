package com.mygitgor.user_service.infrastructure.kafka.event;

import java.time.LocalDateTime;

public record CartUpdatedEvent(
        String cartId,
        String userId,
        Integer totalItems,
        Double subtotal,
        LocalDateTime occurredAt
) {}