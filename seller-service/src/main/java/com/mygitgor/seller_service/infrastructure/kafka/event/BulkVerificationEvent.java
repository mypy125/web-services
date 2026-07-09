package com.mygitgor.seller_service.infrastructure.kafka.event;

import java.time.LocalDateTime;
import java.util.List;

public record BulkVerificationEvent(
        List<String> sellerIds,
        String verifiedBy,
        LocalDateTime timestamp
) {}