package com.mygitgor.auth_service.application.dto.response.seller;

import com.mygitgor.auth_service.application.dto.common.AddressDto;
import com.mygitgor.auth_service.application.dto.common.BankDetailsDto;
import com.mygitgor.auth_service.application.dto.common.BusinessDetailsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Seller information response")
public class SellerInfoResponseDto {

    @Schema(description = "Seller ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sellerId;

    @Schema(description = "Seller name", example = "TechStore")
    private String sellerName;

    @Schema(description = "Email address", example = "seller@techstore.com")
    private String email;

    @Schema(description = "Mobile number", example = "+1234567890")
    private String mobile;

    @Schema(description = "Business details")
    private BusinessDetailsDto businessDetails;

    @Schema(description = "Bank details")
    private BankDetailsDto bankDetails;

    @Schema(description = "Pickup address")
    private AddressDto pickupAddress;

    @Schema(description = "GST/VAT number", example = "22AAAAA0000A1Z")
    private String gstNumber;

    @Schema(description = "PAN number", example = "ABCDE1234F")
    private String panNumber;

    @Schema(description = "Is email verified", example = "true")
    private boolean emailVerified;

    @Schema(description = "Account status", example = "ACTIVE")
    private AccountStatus accountStatus;

    @Schema(description = "Total products count", example = "150")
    private Integer totalProducts;

    @Schema(description = "Total orders count", example = "500")
    private Integer totalOrders;

    @Schema(description = "Rating", example = "4.5")
    private Double rating;

    @Schema(description = "Joined at")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated at")
    private LocalDateTime updatedAt;
}
