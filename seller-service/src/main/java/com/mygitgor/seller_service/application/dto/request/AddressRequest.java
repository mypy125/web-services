package com.mygitgor.seller_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Address request DTO")
public record AddressRequest(

        @Schema(description = "Address name", example = "Main Warehouse")
        String name,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
        @Schema(description = "Address line 1", example = "123 Main Street")
        String addressLine1,

        @Schema(description = "Address line 2", example = "Suite 100")
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        @Schema(description = "City", example = "New York")
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State must not exceed 100 characters")
        @Schema(description = "State", example = "NY")
        String state,

        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "^[0-9]{5,10}$", message = "Invalid postal code format")
        @Schema(description = "Postal code", example = "10001")
        String postalCode,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must not exceed 100 characters")
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

        @Schema(description = "Is default address", example = "true")
        boolean isDefault,

        @Schema(description = "Additional instructions", example = "Ring the doorbell")
        String additionalInstructions
) {

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isBlank();
    }
}