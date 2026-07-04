package com.mygitgor.seller_service.infrastructure.kafka.event;

import com.mygitgor.seller_service.domain.model.status.AccountStatus;
import com.mygitgor.seller_service.domain.model.status.SellerVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Event emitted when a seller registers")
public record SellerRegisteredEvent(

        @Schema(description = "Seller ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String sellerId,

        @Schema(description = "Seller email", example = "seller@example.com")
        String email,

        @Schema(description = "Seller name", example = "Tech Store")
        String sellerName,

        @Schema(description = "Store name", example = "Tech Store")
        String storeName,

        @Schema(description = "Account status", example = "PENDING_VERIFICATION")
        AccountStatus accountStatus,

        @Schema(description = "Verification status", example = "PENDING")
        SellerVerificationStatus verificationStatus,

        @Schema(description = "Mobile number", example = "+1234567890")
        String mobile,

        @Schema(description = "Business name", example = "Tech Store LLC")
        String businessName,

        @Schema(description = "Event timestamp")
        LocalDateTime occurredAt
) {}
