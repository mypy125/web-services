package com.mygitgor.seller_service.infrastructure.kafka.event;

import com.mygitgor.seller_service.domain.model.SellerReport;

import java.time.LocalDateTime;

public record SellerReportGeneratedEvent(
        String reportId,
        String sellerId,
        SellerReport report,
        LocalDateTime timestamp
) {}
