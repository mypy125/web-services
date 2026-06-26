package com.mygitgor.seller_service.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Address response DTO")
public record AddressResponse(
        @Schema(description = "Address ID", example = "addr-123e4567-e89b-12d3-a456-426614174000")
        String id,

        @Schema(description = "Seller ID", example = "seller-123e4567-e89b-12d3-a456-426614174000")
        String sellerId,

        @Schema(description = "Address name", example = "Main Warehouse")
        String name,

        @Schema(description = "Address line 1", example = "123 Main Street")
        String addressLine1,

        @Schema(description = "Address line 2", example = "Suite 100")
        String addressLine2,

        @Schema(description = "City", example = "New York")
        String city,

        @Schema(description = "State", example = "NY")
        String state,

        @Schema(description = "Postal code", example = "10001")
        String postalCode,

        @Schema(description = "Country", example = "USA")
        String country,

        @Schema(description = "Phone number", example = "+1234567890")
        String phoneNumber,

        @Schema(description = "Landmark", example = "Near Central Park")
        String landmark,

        @Schema(description = "Latitude", example = "40.7128")
        Double latitude,

        @Schema(description = "Longitude", example = "-74.0060")
        Double longitude,

        @Schema(description = "Address type", example = "PICKUP",
                allowableValues = {"PICKUP", "RETURN", "WAREHOUSE", "OFFICE",
                        "SHIPPING", "BILLING", "REGISTERED_OFFICE",
                        "BRANCH_OFFICE", "STORE", "SHOWROOM", "OTHER"})
        String addressType,

        @Schema(description = "Address type display name", example = "Адрес самовывоза")
        String addressTypeDisplayName,

        @Schema(description = "Is default address", example = "true")
        boolean isDefault,

        @Schema(description = "Additional instructions", example = "Ring the doorbell")
        String additionalInstructions,

        @Schema(description = "Created at")
        LocalDateTime createdAt,

        @Schema(description = "Updated at")
        LocalDateTime updatedAt,

        @Schema(description = "Full address", example = "123 Main Street, Suite 100, New York, NY 10001, USA")
        String fullAddress,

        @Schema(description = "Short address", example = "123 Main St, New York, NY")
        String shortAddress
) {

    public AddressResponse {
        if (fullAddress == null) {
            fullAddress = buildFullAddress(addressLine1, addressLine2, city, state, postalCode, country);
        }
        if (shortAddress == null) {
            shortAddress = buildShortAddress(addressLine1, city, state);
        }
    }

    public boolean isPickup() { return "PICKUP".equals(addressType); }
    public boolean isReturn() { return "RETURN".equals(addressType); }
    public boolean isWarehouse() { return "WAREHOUSE".equals(addressType); }
    public boolean isOffice() {
        return "OFFICE".equals(addressType) ||
                "REGISTERED_OFFICE".equals(addressType) ||
                "BRANCH_OFFICE".equals(addressType);
    }

    private static String buildFullAddress(String line1, String line2, String city, String state, String zip, String country) {
        StringBuilder sb = new StringBuilder();
        if (line1 != null) sb.append(line1);
        if (line2 != null && !line2.isBlank()) sb.append(", ").append(line2);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        if (zip != null) sb.append(" ").append(zip);
        if (country != null) sb.append(", ").append(country);
        return sb.toString();
    }

    private static String buildShortAddress(String line1, String city, String state) {
        StringBuilder sb = new StringBuilder();
        if (line1 != null) {
            sb.append(line1.length() > 20 ? line1.substring(0, 20) + "..." : line1);
        }
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        return sb.toString();
    }
}