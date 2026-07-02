package com.mygitgor.user_service.application.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Order status statistics DTO")
public record OrderStatusStatisticsDto(
        @Schema(description = "User ID", example = "usr-123")
        String userId,

        @Schema(description = "Map of status counts")
        Map<String, Integer> statusCounts,

        @Schema(description = "Pending orders count", example = "3")
        Integer pendingOrders,

        @Schema(description = "Processing orders count", example = "2")
        Integer processingOrders,

        @Schema(description = "Shipped orders count", example = "5")
        Integer shippedOrders,

        @Schema(description = "Delivered orders count", example = "25")
        Integer deliveredOrders,

        @Schema(description = "Cancelled orders count", example = "4")
        Integer cancelledOrders,

        @Schema(description = "Refunded orders count", example = "3")
        Integer refundedOrders,

        @Schema(description = "Active orders count", example = "10")
        Integer activeOrders,

        @Schema(description = "Completed orders count", example = "28")
        Integer completedOrders,

        @Schema(description = "Completion rate percentage", example = "73.68")
        Double completionRate
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private Map<String, Integer> statusCounts;
        private Integer pendingOrders;
        private Integer processingOrders;
        private Integer shippedOrders;
        private Integer deliveredOrders;
        private Integer cancelledOrders;
        private Integer refundedOrders;
        private Integer activeOrders;
        private Integer completedOrders;
        private Double completionRate;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder statusCounts(Map<String, Integer> statusCounts) {
            this.statusCounts = statusCounts;
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

        public Builder cancelledOrders(Integer cancelledOrders) {
            this.cancelledOrders = cancelledOrders;
            return this;
        }

        public Builder refundedOrders(Integer refundedOrders) {
            this.refundedOrders = refundedOrders;
            return this;
        }

        public Builder activeOrders(Integer activeOrders) {
            this.activeOrders = activeOrders;
            return this;
        }

        public Builder completedOrders(Integer completedOrders) {
            this.completedOrders = completedOrders;
            return this;
        }

        public Builder completionRate(Double completionRate) {
            this.completionRate = completionRate;
            return this;
        }

        public OrderStatusStatisticsDto build() {
            return new OrderStatusStatisticsDto(
                    userId,
                    statusCounts,
                    pendingOrders,
                    processingOrders,
                    shippedOrders,
                    deliveredOrders,
                    cancelledOrders,
                    refundedOrders,
                    activeOrders,
                    completedOrders,
                    completionRate
            );
        }
    }
}
