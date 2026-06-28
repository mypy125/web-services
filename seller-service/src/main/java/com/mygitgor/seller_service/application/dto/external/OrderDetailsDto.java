package com.mygitgor.seller_service.application.dto.external;

import com.mygitgor.seller_service.shared.external.Currency;
import com.mygitgor.seller_service.shared.external.DeliveryStatus;
import com.mygitgor.seller_service.shared.external.OrderStatus;
import com.mygitgor.seller_service.shared.external.PaymentStatus;
import lombok.Builder;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Builder
@Schema(description = "Order details DTO represented as an immutable record")
public record OrderDetailsDto(
        @Schema(description = "Order ID", example = "ord-123e4567-e89b-12d3-a456-426614174000")
        String id,

        @Schema(description = "Order number", example = "ORD-2024-001")
        String orderNumber,

        @Schema(description = "Customer ID", example = "usr-123e4567-e89b-12d3-a456-426614174000")
        String customerId,

        @Schema(description = "Seller ID", example = "sel-123e4567-e89b-12d3-a456-426614174000")
        String sellerId,

        @Schema(description = "Product ID", example = "prd-123e4567-e89b-12d3-a456-426614174000")
        String productId,

        @Schema(description = "Product name", example = "iPhone 15 Pro")
        String productName,

        @Schema(description = "Product category", example = "Electronics")
        String category,

        @Schema(description = "Quantity", example = "2")
        Integer quantity,

        @Schema(description = "Unit price", example = "999.99")
        Double unitPrice,

        @Schema(description = "Total amount", example = "1999.98")
        Double totalAmount,

        @Schema(description = "Subtotal", example = "1999.98")
        Double subtotal,

        @Schema(description = "Tax", example = "199.99")
        Double tax,

        @Schema(description = "Shipping cost", example = "10.00")
        Double shippingCost,

        @Schema(description = "Discount", example = "50.00")
        Double discount,

        @Schema(description = "Commission", example = "100.00")
        Double commission,

        @Schema(description = "Net earnings", example = "1059.99")
        Double netEarnings,

        @Schema(description = "Order status", example = "DELIVERED")
        OrderStatus status,

        @Schema(description = "Payment status", example = "PAID")
        PaymentStatus paymentStatus,

        @Schema(description = "Delivery status", example = "DELIVERED")
        DeliveryStatus deliveryStatus,

        @Schema(description = "Currency", example = "USD")
        Currency currency,

        @Schema(description = "Is new customer", example = "true")
        boolean isNewCustomer,

        @Schema(description = "Is first purchase", example = "true")
        boolean isFirstPurchase,

        @Schema(description = "Customer email", example = "customer@example.com")
        String customerEmail,

        @Schema(description = "Customer name", example = "John Doe")
        String customerName,

        @Schema(description = "Delivery address")
        AddressDto deliveryAddress,

        @Schema(description = "Billing address")
        AddressDto billingAddress,

        @Schema(description = "Created at")
        LocalDateTime createdAt,

        @Schema(description = "Updated at")
        LocalDateTime updatedAt,

        @Schema(description = "Completed at")
        LocalDateTime completedAt,

        @Schema(description = "Order items")
        List<OrderItemDto> orderItems
) {

    @Builder
    @Schema(description = "Order item DTO represented as an immutable record")
    public record OrderItemDto(
            @Schema(description = "Product ID", example = "prd-123e4567-e89b-12d3-a456-426614174000")
            String productId,

            @Schema(description = "Product name", example = "iPhone 15 Pro")
            String productName,

            @Schema(description = "Category", example = "Electronics")
            String category,

            @Schema(description = "Quantity", example = "2")
            Integer quantity,

            @Schema(description = "Unit price", example = "999.99")
            Double unitPrice,

            @Schema(description = "Total price", example = "1999.98")
            Double totalPrice,

            @Schema(description = "SKU", example = "IP15PRO-001")
            String sku
    ) {}

    @Builder
    @Schema(description = "Address DTO represented as an immutable record")
    public record AddressDto(
            @Schema(description = "Address line 1", example = "123 Main St")
            String addressLine1,

            @Schema(description = "Address line 2", example = "Apt 4B")
            String addressLine2,

            @Schema(description = "City", example = "New York")
            String city,

            @Schema(description = "State", example = "NY")
            String state,

            @Schema(description = "Postal code", example = "10001")
            String postalCode,

            @Schema(description = "Country", example = "USA")
            String country
    ) {}
}