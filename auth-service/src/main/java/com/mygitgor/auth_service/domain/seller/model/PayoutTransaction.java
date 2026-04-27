package com.mygitgor.auth_service.domain.seller.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PayoutTransaction {
    private final String transactionId;
    private final String sellerId;
    private final double amount;
    private final double commission;
    private final double netAmount;
    private final String status;
    private final String bankReference;
    private final LocalDateTime initiatedAt;
    private final LocalDateTime completedAt;
    private final String failureReason;
}
