package com.mygitgor.seller_service.application.dto.external;

import com.mygitgor.seller_service.domain.model.type.BankAccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Bank details Request DTO")
public record BankDetailsDto(

        @NotBlank(message = "Account number is required")
        @Size(min = 9, max = 18, message = "Account number must be between 9 and 18 digits")
        @Pattern(regexp = "^[0-9]+$", message = "Account number must contain only digits")
        @Schema(description = "Account number", example = "123456789012")
        String accountNumber,

        @NotBlank(message = "Account holder name is required")
        @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
        @Schema(description = "Account holder name", example = "John Doe")
        String accountHolderName,

        @NotBlank(message = "Bank name is required")
        @Size(min = 2, max = 100, message = "Bank name must be between 2 and 100 characters")
        @Schema(description = "Bank name", example = "Chase Bank")
        String bankName,

        @NotBlank(message = "Bank code is required")
        @Schema(description = "Bank code", example = "CHASUS33")
        String bankCode,

        @Schema(description = "Branch name", example = "Main Branch")
        String branchName,

        @Schema(description = "Account type", example = "CHECKING")
        BankAccountType accountType,

        @Schema(description = "UPI ID", example = "username@bankname")
        String upiId,

        @Schema(description = "IFSC code", example = "CHAS0IN1234")
        String ifscCode,

        @Schema(description = "SWIFT code", example = "CHASUS33")
        String swiftCode
) {}
