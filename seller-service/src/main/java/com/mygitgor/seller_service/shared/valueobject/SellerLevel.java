package com.mygitgor.seller_service.shared.valueobject;

import lombok.Getter;

@Getter
public enum SellerLevel {
    BRONZE("Bronze", 0, 100),
    SILVER("Silver", 101, 499),
    GOLD("Gold", 500, 999),
    PLATINUM("Platinum", 1000, Integer.MAX_VALUE);

    private final String displayName;
    private final int minOrders;
    private final int maxOrders;

    SellerLevel(String displayName, int minOrders, int maxOrders) {
        this.displayName = displayName;
        this.minOrders = minOrders;
        this.maxOrders = maxOrders;
    }

    public boolean isAtLeast(SellerLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
