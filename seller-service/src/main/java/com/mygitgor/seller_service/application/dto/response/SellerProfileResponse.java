package com.mygitgor.seller_service.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mygitgor.seller_service.domain.model.shared.valueobject.*;
import com.mygitgor.seller_service.domain.model.shared.valueobject.AccountStatus;
import com.mygitgor.seller_service.domain.model.shared.valueobject.SellerLevel;
import com.mygitgor.seller_service.domain.model.shared.valueobject.SellerVerificationStatus;
import com.mygitgor.seller_service.domain.model.shared.valueobject.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Seller profile response")
public record SellerProfileResponse(

        @Schema(description = "Seller ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Seller email", example = "seller@example.com")
        String email,

        @Schema(description = "Seller name", example = "Tech Store")
        String sellerName,

        @Schema(description = "Store name", example = "Tech Store")
        String fullStoreName,

        @Schema(description = "Display name", example = "Tech Store")
        String displayName,

        @Schema(description = "Mobile number", example = "+1234567890")
        String mobile,

        @Schema(description = "Phone number", example = "+1234567890")
        String phoneNumber,

        @Schema(description = "Profile image URL", example = "https://example.com/profile.jpg")
        String profileImage,

        @Schema(description = "Cover image URL", example = "https://example.com/cover.jpg")
        String coverImage,

        @Schema(description = "Business details")
        BusinessDetails businessDetails,

        @Schema(description = "Bank details")
        BankDetails bankDetails,

        @Schema(description = "Pickup address")
        Address pickupAddress,

        @Schema(description = "Return address")
        Address returnAddress,

        @Schema(description = "Warehouse addresses")
        List<Address> warehouseAddresses,

        @Schema(description = "GST number", example = "22AAAAA0000A1Z5")
        String gstNumber,

        @Schema(description = "PAN number", example = "ABCDE1234F")
        String panNumber,

        @Schema(description = "TIN number", example = "TIN123456")
        String tinNumber,

        @Schema(description = "Account status", example = "ACTIVE")
        AccountStatus accountStatus,

        @Schema(description = "Verification status", example = "FULLY_VERIFIED")
        SellerVerificationStatus verificationStatus,

        @Schema(description = "Can sell", example = "true")
        boolean canSell,

        @Schema(description = "Can add products", example = "true")
        boolean canAddProducts,

        @Schema(description = "Status display name", example = "Active")
        String statusDisplayName,

        @Schema(description = "Store description", example = "Best electronics store")
        String storeDescription,

        @Schema(description = "Store tagline", example = "Your one-stop shop for electronics")
        String storeTagline,

        @Schema(description = "Store website", example = "https://techstore.com")
        String storeWebsite,

        @Schema(description = "Store email", example = "store@techstore.com")
        String storeEmail,

        @Schema(description = "Store phone", example = "+1234567890")
        String storePhone,

        @Schema(description = "Store category", example = "ELECTRONICS")
        StoreCategory storeCategory,

        @Schema(description = "Store categories")
        List<StoreCategory> storeCategories,

        @Schema(description = "Average rating", example = "4.5")
        Double averageRating,

        @Schema(description = "Total reviews", example = "150")
        Integer totalReviews,

        @Schema(description = "Rating display", example = "4.5")
        String ratingDisplay,

        @Schema(description = "Seller level", example = "GOLD")
        SellerLevel sellerLevel,

        @Schema(description = "Created at")
        LocalDateTime createdAt,

        @Schema(description = "Updated at")
        LocalDateTime updatedAt,

        @Schema(description = "Last login at")
        LocalDateTime lastLoginAt,

        @Schema(description = "Email verified at")
        LocalDateTime emailVerifiedAt,

        @Schema(description = "Business verified at")
        LocalDateTime businessVerifiedAt
) {}
