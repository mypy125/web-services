package com.mygitgor.auth_service.application.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Business information")
public class BusinessDetailsDto {

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    @Schema(description = "Registered business name", example = "Tech Store LLC")
    private String businessName;

    @NotBlank(message = "Business email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Business email address", example = "contact@techstore.com")
    private String businessEmail;

    @NotBlank(message = "Business mobile is required")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid mobile number format")
    @Schema(description = "Business contact number", example = "+1234567890")
    private String businessMobile;

    @NotBlank(message = "Business address is required")
    @Size(max = 255, message = "Business address must not exceed 255 characters")
    @Schema(description = "Business address", example = "123 Business Park")
    private String businessAddress;

    @Schema(description = "Business logo URL", example = "https://example.com/logo.png")
    private String logo;

    @Schema(description = "Business banner URL", example = "https://example.com/banner.png")
    private String banner;

    @Schema(description = "Business registration number", example = "REG123456")
    private String registrationNumber;

    @Schema(description = "Tax identification number", example = "TIN123456")
    private String taxId;

    @Schema(description = "Website URL", example = "https://www.techstore.com")
    private String website;

    @Schema(description = "Business description", example = "Electronics and gadgets store")
    private String description;
}
