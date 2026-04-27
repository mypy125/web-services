package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutTransactionDto {
    private String transactionId;
    private String sellerId;
    private double amount;
    private double commission;
    private double netAmount;
    private String status;
    private String bankReference;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private String failureReason;
}
