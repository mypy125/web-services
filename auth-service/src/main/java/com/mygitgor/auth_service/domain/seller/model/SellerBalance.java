package com.mygitgor.auth_service.domain.seller.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SellerBalance {
    private final String sellerId;
    private final double availableBalance;
    private final double pendingBalance;
    private final double totalEarned;
    private final double totalWithdrawn;
    private final double totalCommission;
    private final LocalDateTime lastPayoutDate;
    private final LocalDateTime nextPayoutDate;
    private final String currency;
}
