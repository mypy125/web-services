package com.mygitgor.seller_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerifyDocumentsRequest(
        @NotBlank(message = "Seller ID cannot be blank")
        String sellerId,

        @NotNull(message = "Approve flag must be explicitly set (true/false)")
        Boolean approve,

        @NotBlank(message = "Validator/Admin identity (verifiedBy) is required")
        String verifiedBy,

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        String notes
) {}
