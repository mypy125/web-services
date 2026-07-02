package com.mygitgor.user_service.application.dto.external;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

public record UserDashboardDto(
        UserProfileDto profile,
        AddressDto defaultAddress,
        Integer cartItemsCount,
        Double cartTotal,
        CartSummaryDto cartSummary,
        Integer totalOrders,
        Double totalSpent,
        Double averageOrderValue,
        LocalDateTime lastOrderDate,
        List<OrderSummaryDto> recentOrders,
        Map<String, Integer> orderStatusCounts,
        Integer availableCouponsCount,
        LocalDateTime lastActiveAt,
        LocalDateTime memberSince
) {

    public boolean hasRecentOrders() {
        return recentOrders != null && !recentOrders.isEmpty();
    }

    public boolean hasCartItems() {
        return cartItemsCount != null && cartItemsCount > 0;
    }

    public boolean hasDefaultAddress() {
        return defaultAddress != null;
    }

    public String getMemberSinceFormatted() {
        if (memberSince == null) return "N/A";
        return memberSince.getYear() + "年 " + memberSince.getMonthValue() + "月";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserProfileDto profile;
        private AddressDto defaultAddress;
        private Integer cartItemsCount;
        private Double cartTotal;
        private CartSummaryDto cartSummary;
        private Integer totalOrders;
        private Double totalSpent;
        private Double averageOrderValue;
        private LocalDateTime lastOrderDate;
        private List<OrderSummaryDto> recentOrders;
        private Map<String, Integer> orderStatusCounts;
        private Integer availableCouponsCount;
        private LocalDateTime lastActiveAt;
        private LocalDateTime memberSince;

        public Builder profile(UserProfileDto profile) {
            this.profile = profile;
            return this;
        }

        public Builder defaultAddress(AddressDto defaultAddress) {
            this.defaultAddress = defaultAddress;
            return this;
        }

        public Builder cartItemsCount(Integer cartItemsCount) {
            this.cartItemsCount = cartItemsCount;
            return this;
        }

        public Builder cartTotal(Double cartTotal) {
            this.cartTotal = cartTotal;
            return this;
        }

        public Builder cartSummary(CartSummaryDto cartSummary) {
            this.cartSummary = cartSummary;
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

        public Builder averageOrderValue(Double averageOrderValue) {
            this.averageOrderValue = averageOrderValue;
            return this;
        }

        public Builder lastOrderDate(LocalDateTime lastOrderDate) {
            this.lastOrderDate = lastOrderDate;
            return this;
        }

        public Builder recentOrders(List<OrderSummaryDto> recentOrders) {
            this.recentOrders = recentOrders;
            return this;
        }

        public Builder orderStatusCounts(Map<String, Integer> orderStatusCounts) {
            this.orderStatusCounts = orderStatusCounts;
            return this;
        }

        public Builder availableCouponsCount(Integer availableCouponsCount) {
            this.availableCouponsCount = availableCouponsCount;
            return this;
        }

        public Builder lastActiveAt(LocalDateTime lastActiveAt) {
            this.lastActiveAt = lastActiveAt;
            return this;
        }

        public Builder memberSince(LocalDateTime memberSince) {
            this.memberSince = memberSince;
            return this;
        }

        public UserDashboardDto build() {
            return new UserDashboardDto(
                    profile,
                    defaultAddress,
                    cartItemsCount,
                    cartTotal,
                    cartSummary,
                    totalOrders,
                    totalSpent,
                    averageOrderValue,
                    lastOrderDate,
                    recentOrders,
                    orderStatusCounts,
                    availableCouponsCount,
                    lastActiveAt,
                    memberSince
            );
        }
    }
}