package com.mygitgor.seller_service.application.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Business details Request DTO")
public record BusinessDetailsDto(

        @NotBlank(message = "Business name is required")
        @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
        @Schema(description = "Business name", example = "Tech Store LLC")
        String businessName,

        @NotBlank(message = "Business email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Business email", example = "business@techstore.com")
        String businessEmail,

        @NotBlank(message = "Business mobile is required")
        @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid mobile number format")
        @Schema(description = "Business mobile", example = "+1234567890")
        String businessMobile,

        @NotBlank(message = "Business address is required")
        @Size(max = 500, message = "Business address must not exceed 500 characters")
        @Schema(description = "Business address", example = "123 Business Park, New York, NY 10001")
        String businessAddress,

        @Schema(description = "Business website", example = "https://techstore.com")
        String businessWebsite,

        @Schema(description = "Business description", example = "Leading electronics retailer")
        String businessDescription,

        @Schema(description = "Business type", example = "RETAIL")
        String businessType,

        @Schema(description = "Year of establishment", example = "2020")
        Integer yearOfEstablishment,

        @Schema(description = "Number of employees", example = "50")
        Integer numberOfEmployees,

        @Schema(description = "Registration number", example = "REG123456")
        String registrationNumber,

        @Schema(description = "Tax ID", example = "TAX123456")
        String taxId,

        @Schema(description = "Business logo URL", example = "https://example.com/logo.png")
        String logo,

        @Schema(description = "Business banner URL", example = "https://example.com/banner.png")
        String banner
) {}
