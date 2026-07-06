package com.mygitgor.transaction_service.domain.model.valueobject;

import lombok.Getter;

@Getter
public enum PayoutType {
    SELLER_PAYOUT("Seller Payout", "Payment to the seller"),
    COMMISSION_PAYOUT("Commission Payout", "Commission payment"),
    BONUS_PAYOUT("Bonus Payout", "Bonus payment"),
    REFUND_PAYOUT("Refund Payout", "Refund payment"),
    ADJUSTMENT_PAYOUT("Adjustment Payout", "Corrective payment");

    private final String displayName;
    private final String description;

    PayoutType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
