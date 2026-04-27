package com.mygitgor.auth_service.application.dto.request.seller;

import com.mygitgor.auth_service.application.dto.common.AddressDto;
import com.mygitgor.auth_service.application.dto.common.BankDetailsDto;
import com.mygitgor.auth_service.application.dto.common.BusinessDetailsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Seller registration request")
public class SellerRegistrationRequestDto {

    @NotBlank(message = "Seller name is required")
    @Size(min = 2, max = 100, message = "Seller name must be between 2 and 100 characters")
    @Schema(description = "Store/seller name", example = "TechStore")
    private String sellerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Seller email address", example = "seller@techstore.com")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid mobile number format")
    @Schema(description = "Mobile number with country code", example = "+1234567890")
    private String mobile;

    @Valid
    @Schema(description = "Business details")
    private BusinessDetailsDto businessDetails;

    @Valid
    @Schema(description = "Bank details for payouts")
    private BankDetailsDto bankDetails;

    @Valid
    @Schema(description = "Pickup address")
    private AddressDto pickupAddress;

    @Schema(description = "GST/VAT number", example = "22AAAAA0000A1Z")
    private String gstNumber;

    @Schema(description = "PAN number", example = "ABCDE1234F")
    private String panNumber;
}
