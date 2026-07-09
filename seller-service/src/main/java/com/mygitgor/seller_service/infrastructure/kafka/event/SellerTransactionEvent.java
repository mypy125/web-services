package com.mygitgor.seller_service.infrastructure.kafka.event;

import com.mygitgor.seller_service.application.dto.external.TransactionDto;

import java.time.LocalDateTime;

public record SellerTransactionEvent(
        String transactionId,
        String sellerId,
        TransactionDto transaction,
        LocalDateTime timestamp
) {}
