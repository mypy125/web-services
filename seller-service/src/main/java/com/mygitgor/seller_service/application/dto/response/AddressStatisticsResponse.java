package com.mygitgor.seller_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Address statistics response")
public record AddressStatisticsResponse(
        @Schema(description = "Total addresses", example = "5")
        Long totalAddresses,

        @Schema(description = "Pickup addresses count", example = "1")
        Long pickupAddressCount,

        @Schema(description = "Return addresses count", example = "1")
        Long returnAddressCount,

        @Schema(description = "Warehouse addresses count", example = "2")
        Long warehouseAddressCount,

        @Schema(description = "Office addresses count", example = "1")
        Long officeAddressCount,

        @Schema(description = "Shipping addresses count", example = "0")
        Long shippingAddressCount,

        @Schema(description = "Billing addresses count", example = "0")
        Long billingAddressCount,

        @Schema(description = "Other addresses count", example = "0")
        Long otherAddressCount,

        @Schema(description = "Addresses by country")
        Map<String, Long> addressesByCountry,

        @Schema(description = "Addresses by city")
        Map<String, Long> addressesByCity,

        @Schema(description = "Addresses by type")
        Map<String, Long> addressesByType,

        @Schema(description = "Default pickup address")
        AddressResponse defaultPickupAddress,

        @Schema(description = "Default return address")
        AddressResponse defaultReturnAddress,

        @Schema(description = "Default shipping address")
        AddressResponse defaultShippingAddress,

        @Schema(description = "Default billing address")
        AddressResponse defaultBillingAddress,

        @Schema(description = "Calculated at")
        LocalDateTime calculatedAt
) {

    public AddressStatisticsResponse {
        totalAddresses = (totalAddresses == null) ? 0L : totalAddresses;
        pickupAddressCount = (pickupAddressCount == null) ? 0L : pickupAddressCount;
        returnAddressCount = (returnAddressCount == null) ? 0L : returnAddressCount;
        warehouseAddressCount = (warehouseAddressCount == null) ? 0L : warehouseAddressCount;
        officeAddressCount = (officeAddressCount == null) ? 0L : officeAddressCount;
        shippingAddressCount = (shippingAddressCount == null) ? 0L : shippingAddressCount;
        billingAddressCount = (billingAddressCount == null) ? 0L : billingAddressCount;
        otherAddressCount = (otherAddressCount == null) ? 0L : otherAddressCount;
        calculatedAt = (calculatedAt == null) ? LocalDateTime.now() : calculatedAt;
    }

    @Schema(description = "Has any address", example = "true")
    public boolean hasAnyAddress() {
        return totalAddresses > 0;
    }

    @Schema(description = "Has pickup address", example = "true")
    public boolean hasPickupAddress() {
        return pickupAddressCount > 0;
    }

    @Schema(description = "Has return address", example = "true")
    public boolean hasReturnAddress() {
        return returnAddressCount > 0;
    }

    @Schema(description = "Has warehouse addresses", example = "true")
    public boolean hasWarehouseAddresses() {
        return warehouseAddressCount > 0;
    }
}