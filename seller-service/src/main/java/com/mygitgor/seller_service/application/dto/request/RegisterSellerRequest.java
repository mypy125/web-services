package com.mygitgor.seller_service.application.dto.request;

import com.mygitgor.seller_service.domain.model.shared.valueobject.Address;
import com.mygitgor.seller_service.domain.model.shared.valueobject.BankDetails;
import com.mygitgor.seller_service.domain.model.shared.valueobject.BusinessDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record RegisterSellerRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Seller name is required")
        String sellerName,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid mobile number format")
        String mobile,

        @NotNull(message = "Business details are required")
        @Valid
        BusinessDetails businessDetails,

        @NotNull(message = "Bank details are required")
        @Valid
        BankDetails bankDetails,

        @NotNull(message = "Pickup address is required")
        @Valid
        Address pickupAddress
) {}