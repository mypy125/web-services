package com.mygitgor.user_service.infrastructure.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Data transfer object representing a saved secure user payment method")
public record PaymentMethodDto(

        @Schema(description = "Unique identifier of the payment method record", example = "pay-3341-xyz")
        @NotBlank(message = "Payment method ID cannot be blank")
        String id,

        @Schema(description = "Identifier of the user who owns this payment method", example = "usr-4412-xyz")
        @NotBlank(message = "User ID cannot be blank")
        String userId,

        @Schema(description = "The broad category of the payment instrument", example = "CARD")
        @NotBlank(message = "Payment type cannot be blank")
        String type,

        @Schema(description = "The last 4 digits of the credit/debit card for UI identification", example = "4242")
        @NotBlank(message = "Last 4 digits cannot be blank")
        @Pattern(regexp = "^\\d{4}$", message = "Last 4 digits must be exactly 4 numerical characters")
        String last4Digits,

        @Schema(description = "The payment card brand/network", example = "VISA")
        String cardType,

        @Schema(description = "Two-digit expiration month", example = "12")
        @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expiry month must be between 01 and 12")
        String expiryMonth,

        @Schema(description = "Four-digit expiration year", example = "2029")
        @Pattern(regexp = "^\\d{4}$", message = "Expiry year must be a 4-digit number")
        String expiryYear,

        @Schema(description = "Flag indicating if this is the user's primary/preferred billing method", example = "true")
        boolean isDefault
) {
    public String getMaskedCardLabel() {
        if (cardType == null || last4Digits == null) return "Unknown Payment Method";
        return String.format("%s •••• %s", cardType, last4Digits);
    }
}
