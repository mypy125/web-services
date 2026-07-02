package com.mygitgor.user_service.application.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data transfer object representing a user's physical or shipping address")
public record AddressDto(

        @Schema(description = "Unique identifier of the address record", example = "addr-7721-qwe")
        @NotBlank(message = "Address ID cannot be blank")
        String id,

        @Schema(description = "Identifier of the user who owns this address", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "Primary address line (street, building, apartment)", example = "123 Main Street, Apt 4B")
        @NotBlank(message = "Address line 1 cannot be blank")
        String addressLine1,

        @Schema(description = "Secondary address line (optional details, business name)", example = "Floor 2, Room 204")
        String addressLine2,

        @Schema(description = "City name", example = "Yerevan")
        @NotBlank(message = "City cannot be blank")
        String city,

        @Schema(description = "State, province, or region", example = "Yerevan District")
        String state,

        @Schema(description = "Postal or ZIP code", example = "0010")
        @NotBlank(message = "Postal code cannot be blank")
        String postalCode,

        @Schema(description = "Country name or ISO country code", example = "Armenia")
        @NotBlank(message = "Country cannot be blank")
        String country,

        @Schema(description = "Flag indicating if this is the user's primary/default shipping address", example = "true")
        boolean isDefault,

        @Schema(description = "The classification of the address", example = "SHIPPING")
        @NotNull(message = "Address type cannot be null")
        String addressType
) {}
