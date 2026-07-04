package com.mygitgor.seller_service.application.dto.external;

import com.mygitgor.seller_service.domain.model.type.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Address Data Transfer Object")
public record AddressDto(

        @Schema(description = "Address name", example = "Main Office")
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

        @Schema(description = "Address type", example = "PICKUP")
        AddressType addressType
) {}
