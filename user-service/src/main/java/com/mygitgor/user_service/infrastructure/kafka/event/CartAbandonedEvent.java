package com.mygitgor.user_service.infrastructure.kafka.event;

import java.time.LocalDateTime;

public record CartAbandonedEvent(
        String cartId,
        String userId,
        LocalDateTime abandonedAt
) {}
