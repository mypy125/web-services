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
public class SellerBalanceDto {
    private String sellerId;
    private double availableBalance;
    private double pendingBalance;
    private double totalEarned;
    private double totalWithdrawn;
    private double totalCommission;
    private LocalDateTime lastPayoutDate;
    private LocalDateTime nextPayoutDate;
    private String currency;
}
