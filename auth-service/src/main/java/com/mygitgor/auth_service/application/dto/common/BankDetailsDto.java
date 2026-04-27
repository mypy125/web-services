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
@Schema(description = "Bank account details for payouts")
public class BankDetailsDto {

    @NotBlank(message = "Account number is required")
    @Size(min = 9, max = 18, message = "Account number must be between 9 and 18 digits")
    @Pattern(regexp = "^[0-9]+$", message = "Account number must contain only digits")
    @Schema(description = "Bank account number", example = "123456789012")
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
    @Schema(description = "Name on bank account", example = "John Doe")
    private String accountHolderName;

    @NotBlank(message = "Bank name is required")
    @Size(min = 2, max = 100, message = "Bank name must be between 2 and 100 characters")
    @Schema(description = "Bank name", example = "Chase Bank")
    private String bankName;

    @NotBlank(message = "IFSC/Bank code is required")
    @Pattern(regexp = "^[A-Za-z]{4}[0-9]{7}$", message = "Invalid IFSC code format")
    @Schema(description = "IFSC/SWIFT/Bank code", example = "CHASUS33")
    private String bankCode;

    @Schema(description = "Account type", example = "CHECKING", allowableValues = {"SAVINGS", "CHECKING", "CURRENT"})
    private String accountType;

    @Schema(description = "UPI ID", example = "username@bankname")
    private String upiId;
}
