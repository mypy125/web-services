package com.mygitgor.seller_service.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mygitgor.seller_service.domain.model.shared.valueobject.AccountStatus;
import com.mygitgor.seller_service.domain.model.shared.valueobject.SellerLevel;
import com.mygitgor.seller_service.domain.model.shared.valueobject.SellerVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response for seller registration")
public record SellerRegistrationResponse(

        @Schema(description = "Seller ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Seller email", example = "seller@example.com")
        String email,

        @Schema(description = "Seller name", example = "Tech Store")
        String sellerName,

        @Schema(description = "Store name", example = "Tech Store")
        String fullStoreName,

        @Schema(description = "Account status", example = "PENDING_VERIFICATION")
        AccountStatus accountStatus,

        @Schema(description = "Verification status", example = "PENDING")
        SellerVerificationStatus verificationStatus,

        @Schema(description = "Seller level", example = "BRONZE")
        SellerLevel sellerLevel,

        @Schema(description = "Rating display", example = "N/A")
        String ratingDisplay,

        @Schema(description = "Commission rate", example = "5.0")
        Double commissionRate,

        @Schema(description = "Registration timestamp")
        LocalDateTime registeredAt,

        @Schema(description = "Success message", example = "Seller registered successfully")
        String message,

        @Schema(description = "Next steps", example = "Please verify your email")
        String nextSteps
) {}
