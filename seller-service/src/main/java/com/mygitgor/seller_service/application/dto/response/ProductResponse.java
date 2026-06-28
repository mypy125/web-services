package com.mygitgor.seller_service.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product response DTO represented as an immutable record")
public record ProductResponse(
        String id,
        String sellerId,
        String title,
        String description,
        String shortDescription,
        String slug,
        String sku,
        String barcode,
        Double mrpPrice,
        Double sellingPrice,
        Double costPerItem,
        Double discountPercent,
        Double discountAmount,
        Double taxRate,
        String currency,
        Integer quantity,
        Integer reservedQuantity,
        Integer soldQuantity,
        Integer minimumStockLevel,
        Integer maximumStockLevel,
        boolean backorderAllowed,
        boolean preorderAllowed,
        String inventoryStatus,
        CategoryResponse category,
        List<CategoryResponse> categories,
        String categoryPath,
        String color,
        List<String> colors,
        String size,
        List<String> sizes,
        String material,
        String weightUnit,
        Double weight,
        String dimensions,
        List<String> images,
        String mainImage,
        String thumbnailImage,
        List<String> videos,
        String status,
        boolean isActive,
        boolean isFeatured,
        boolean isNew,
        boolean isBestSeller,
        boolean isOnSale,
        boolean isDigital,
        boolean isPhysical,
        boolean isReturnable,
        Integer returnPeriodDays,
        String shippingInfo,
        Double shippingWeight,
        Double shippingCost,
        boolean freeShipping,
        String deliveryTime,
        Integer numRatings,
        Double averageRating,
        Integer totalReviews,
        Integer positiveReviews,
        Integer neutralReviews,
        Integer negativeReviews,
        String metaTitle,
        String metaDescription,
        String metaKeywords,
        List<String> tags,
        Map<String, String> attributes,
        Map<String, String> specifications,
        String warrantyInfo,
        String returnPolicy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt,
        Double profitMargin,
        Double totalValue,
        boolean inStock,
        boolean lowStock,
        boolean outOfStock
) {

    @Override
    public boolean inStock() {
        return inStock || (quantity != null && quantity > 0);
    }

    @Override
    public boolean lowStock() {
        if (lowStock) return true;
        if (quantity != null && minimumStockLevel != null) {
            return quantity <= minimumStockLevel && quantity > 0;
        }
        return false;
    }

    @Override
    public boolean outOfStock() {
        return outOfStock || (quantity != null && quantity <= 0);
    }

    @Override
    public Double totalValue() {
        if (totalValue != null) return totalValue;
        if (sellingPrice != null && quantity != null) {
            return sellingPrice * quantity;
        }
        return 0.0;
    }

    @Override
    public Double profitMargin() {
        if (profitMargin != null) return profitMargin;
        if (sellingPrice != null && costPerItem != null && sellingPrice > 0) {
            return ((sellingPrice - costPerItem) / sellingPrice) * 100.0;
        }
        return 0.0;
    }
}