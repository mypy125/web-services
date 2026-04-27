package com.mygitgor.auth_service.application.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address information")
public class AddressDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Recipient name", example = "John Doe")
    private String name;

    @NotBlank(message = "Locality is required")
    @Size(max = 100, message = "Locality must not exceed 100 characters")
    @Schema(description = "Locality/area", example = "Downtown")
    private String locality;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(description = "Street address", example = "123 Main Street")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must not exceed 50 characters")
    @Schema(description = "City", example = "New York")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must not exceed 50 characters")
    @Schema(description = "State", example = "NY")
    private String state;

    @NotBlank(message = "Pin code is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pin code format")
    @Schema(description = "Postal/Pin code", example = "10001")
    private String pinCode;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid mobile number format")
    @Schema(description = "Contact mobile number", example = "+1234567890")
    private String mobile;

    @Schema(description = "Address type", example = "PICKUP", allowableValues = {"PICKUP", "BILLING", "SHIPPING"})
    private String addressType;

    @Schema(description = "Is this default address")
    private boolean isDefault;
}
