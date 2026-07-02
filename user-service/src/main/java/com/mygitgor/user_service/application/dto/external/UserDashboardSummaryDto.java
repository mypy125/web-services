package com.mygitgor.user_service.application.dto.external;

import java.time.LocalDateTime;

public record UserDashboardSummaryDto(
        Integer cartItemsCount,
        Integer totalOrders,
        Double totalSpent,
        Integer pendingOrders,
        Integer processingOrders,
        Integer shippedOrders,
        Integer deliveredOrders,
        LocalDateTime lastOrderDate,
        Integer availableCouponsCount
) {

    public boolean hasActiveOrders() {
        return (pendingOrders != null && pendingOrders > 0) ||
                (processingOrders != null && processingOrders > 0) ||
                (shippedOrders != null && shippedOrders > 0);
    }

    public boolean hasCartItems() {
        return cartItemsCount != null && cartItemsCount > 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer cartItemsCount;
        private Integer totalOrders;
        private Double totalSpent;
        private Integer pendingOrders;
        private Integer processingOrders;
        private Integer shippedOrders;
        private Integer deliveredOrders;
        private LocalDateTime lastOrderDate;
        private Integer availableCouponsCount;

        public Builder cartItemsCount(Integer cartItemsCount) {
            this.cartItemsCount = cartItemsCount;
            return this;
        }

        public Builder totalOrders(Integer totalOrders) {
            this.totalOrders = totalOrders;
            return this;
        }

        public Builder totalSpent(Double totalSpent) {
            this.totalSpent = totalSpent;
            return this;
        }

        public Builder pendingOrders(Integer pendingOrders) {
            this.pendingOrders = pendingOrders;
            return this;
        }

        public Builder processingOrders(Integer processingOrders) {
            this.processingOrders = processingOrders;
            return this;
        }

        public Builder shippedOrders(Integer shippedOrders) {
            this.shippedOrders = shippedOrders;
            return this;
        }

        public Builder deliveredOrders(Integer deliveredOrders) {
            this.deliveredOrders = deliveredOrders;
            return this;
        }

        public Builder lastOrderDate(LocalDateTime lastOrderDate) {
            this.lastOrderDate = lastOrderDate;
            return this;
        }

        public Builder availableCouponsCount(Integer availableCouponsCount) {
            this.availableCouponsCount = availableCouponsCount;
            return this;
        }

        public UserDashboardSummaryDto build() {
            return new UserDashboardSummaryDto(
                    cartItemsCount,
                    totalOrders,
                    totalSpent,
                    pendingOrders,
                    processingOrders,
                    shippedOrders,
                    deliveredOrders,
                    lastOrderDate,
                    availableCouponsCount
            );
        }
    }
}