package com.mygitgor.seller_service.application.dto.external;

import com.mygitgor.seller_service.shared.external.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "Product summary DTO represented as an immutable record")
public record ProductSummaryDto(
        @Schema(description = "Product ID", example = "prd-123e4567-e89b-12d3-a456-426614174000")
        String id,

        @Schema(description = "Seller ID", example = "sel-123e4567-e89b-12d3-a456-426614174000")
        String sellerId,

        @Schema(description = "Product name", example = "iPhone 15 Pro")
        String name,

        @Schema(description = "Product description", example = "Latest iPhone with advanced features")
        String description,

        @Schema(description = "Category", example = "Electronics")
        String category,

        @Schema(description = "Sub-category", example = "Smartphones")
        String subCategory,

        @Schema(description = "Brand", example = "Apple")
        String brand,

        @Schema(description = "Price", example = "999.99")
        Double price,

        @Schema(description = "Compare at price", example = "1099.99")
        Double compareAtPrice,

        @Schema(description = "Cost per item", example = "700.00")
        Double costPerItem,

        @Schema(description = "Profit margin", example = "29.9")
        Double profitMargin,

        @Schema(description = "Currency", example = "USD")
        Currency currency,

        @Schema(description = "Available quantity", example = "50")
        Integer availableQuantity,

        @Schema(description = "Total quantity sold", example = "150")
        Integer totalQuantitySold,

        @Schema(description = "Total revenue", example = "149998.50")
        Double totalRevenue,

        @Schema(description = "Average rating", example = "4.5")
        Double averageRating,

        @Schema(description = "Total reviews", example = "80")
        Integer totalReviews,

        @Schema(description = "Is active", example = "true")
        boolean isActive,

        @Schema(description = "Is in stock", example = "true")
        boolean inStock,

        @Schema(description = "Is featured", example = "true")
        boolean isFeatured,

        @Schema(description = "SKU", example = "IP15PRO-001")
        String sku,

        @Schema(description = "Barcode", example = "1234567890123")
        String barcode,

        @Schema(description = "Weight", example = "0.5")
        Double weight,

        @Schema(description = "Dimensions", example = "15cm x 7cm x 1cm")
        String dimensions,

        @Schema(description = "Main image URL", example = "https://example.com/product.jpg")
        String mainImageUrl,

        @Schema(description = "Image URLs", example = "[\"url1.jpg\", \"url2.jpg\"]")
        List<String> imageUrls,

        @Schema(description = "Tags", example = "[\"phone\", \"apple\", \"smartphone\"]")
        List<String> tags,

        @Schema(description = "Created at")
        LocalDateTime createdAt,

        @Schema(description = "Updated at")
        LocalDateTime updatedAt
) {}