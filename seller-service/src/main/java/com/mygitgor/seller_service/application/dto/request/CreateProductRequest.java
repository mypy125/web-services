package com.mygitgor.seller_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@Schema(description = "Create product request DTO represented as an immutable record")
public record CreateProductRequest(
        @NotBlank(message = "Product title is required")
        @Size(min = 3, max = 255, message = "Product title must be between 3 and 255 characters")
        @Schema(description = "Product title", example = "iPhone 15 Pro Max", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        @Schema(description = "Product description", example = "The latest iPhone with advanced features")
        String description,

        @Size(max = 500, message = "Short description cannot exceed 500 characters")
        @Schema(description = "Short product description", example = "Latest flagship smartphone")
        String shortDescription,

        @Schema(description = "Product SKU", example = "IP15PRO-001")
        String sku,

        @Schema(description = "Product barcode", example = "1234567890123")
        String barcode,

        @NotNull(message = "MRP price is required")
        @Positive(message = "MRP price must be positive")
        @Schema(description = "MRP price", example = "1099.99", requiredMode = Schema.RequiredMode.REQUIRED)
        Double mrpPrice,

        @NotNull(message = "Selling price is required")
        @Positive(message = "Selling price must be positive")
        @Schema(description = "Selling price", example = "999.99", requiredMode = Schema.RequiredMode.REQUIRED)
        Double sellingPrice,

        @Schema(description = "Cost per item", example = "700.00")
        Double costPerItem,

        @Schema(description = "Discount percent", example = "10.0")
        @Min(value = 0, message = "Discount percent must be between 0 and 100")
        @Max(value = 100, message = "Discount percent must be between 0 and 100")
        Double discountPercent,

        @Schema(description = "Tax rate", example = "10.0")
        Double taxRate,

        @Schema(description = "Currency", example = "USD")
        String currency,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be at least 0")
        @Schema(description = "Available quantity", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer quantity,

        @Schema(description = "Minimum stock level", example = "10")
        Integer minimumStockLevel,

        @Schema(description = "Maximum stock level", example = "1000")
        Integer maximumStockLevel,

        @Schema(description = "Allow backorder", example = "false")
        boolean backorderAllowed,

        @Schema(description = "Allow preorder", example = "false")
        boolean preorderAllowed,

        @Schema(description = "Preorder available from")
        LocalDateTime preorderAvailableFrom,

        @Schema(description = "Category ID", example = "cat-123e4567-e89b-12d3-a456-426614174000")
        String categoryId,

        @Schema(description = "Category IDs", example = "[\"cat-123\", \"cat-456\"]")
        List<String> categoryIds,

        @Schema(description = "Color", example = "Space Gray")
        String color,

        @Schema(description = "Colors", example = "[\"Space Gray\", \"Silver\", \"Gold\"]")
        List<String> colors,

        @Schema(description = "Size", example = "M")
        String size,

        @Schema(description = "Sizes", example = "[\"S\", \"M\", \"L\", \"XL\"]")
        List<String> sizes,

        @Schema(description = "Material", example = "Aluminum")
        String material,

        @Schema(description = "Pattern", example = "Solid")
        String pattern,

        @Schema(description = "Weight", example = "0.5")
        Double weight,

        @Schema(description = "Weight unit", example = "kg")
        String weightUnit,

        @Schema(description = "Dimensions", example = "15cm x 7cm x 1cm")
        String dimensions,

        @Schema(description = "Main image URL", example = "https://example.com/product-main.jpg")
        String mainImage,

        @Schema(description = "Image URLs", example = "[\"https://example.com/img1.jpg\", \"https://example.com/img2.jpg\"]")
        List<String> images,

        @Schema(description = "Video URLs", example = "[\"https://example.com/video1.mp4\"]")
        List<String> videos,

        @Schema(description = "Shipping info", example = "Ships in 2-3 days")
        String shippingInfo,

        @Schema(description = "Shipping weight", example = "0.5")
        Double shippingWeight,

        @Schema(description = "Shipping cost", example = "10.00")
        Double shippingCost,

        @Schema(description = "Free shipping", example = "false")
        boolean freeShipping,

        @Schema(description = "Delivery time", example = "3-5 business days")
        String deliveryTime,

        @Schema(description = "Delivery area", example = "Worldwide")
        String deliveryArea,

        @Schema(description = "Is returnable", example = "true")
        boolean isReturnable,

        @Schema(description = "Return period days", example = "30")
        Integer returnPeriodDays,

        @Schema(description = "Warranty info", example = "1 year manufacturer warranty")
        String warrantyInfo,

        @Schema(description = "Return policy", example = "30 days return policy")
        String returnPolicy,

        @Schema(description = "Return instructions", example = "Please include original packaging")
        String returnInstructions,

        @Schema(description = "Is digital product", example = "false")
        boolean isDigital,

        @Schema(description = "Is physical product", example = "true")
        boolean isPhysical,

        @Schema(description = "Is bundle", example = "false")
        boolean isBundle,

        @Schema(description = "Is customizable", example = "false")
        boolean isCustomizable,

        @Schema(description = "Meta title", example = "iPhone 15 Pro Max - Best Deals")
        String metaTitle,

        @Schema(description = "Meta description", example = "Buy iPhone 15 Pro Max at best price")
        String metaDescription,

        @Schema(description = "Meta keywords", example = "iPhone, Apple, Smartphone")
        String metaKeywords,

        @Schema(description = "Product tags", example = "[\"phone\", \"apple\", \"smartphone\"]")
        List<String> tags,

        @Schema(description = "Product attributes", example = "{\"brand\": \"Apple\", \"model\": \"iPhone 15 Pro Max\"}")
        Map<String, String> attributes,

        @Schema(description = "Product specifications", example = "{\"processor\": \"A17 Pro\", \"display\": \"6.7-inch OLED\"}")
        Map<String, String> specifications,

        @Schema(description = "Product status", example = "DRAFT")
        String status,

        @Schema(description = "Is featured", example = "false")
        boolean isFeatured
) {

    public boolean hasValidPrice() {
        if (mrpPrice == null || sellingPrice == null) return false;
        if (sellingPrice > mrpPrice) return false;
        if (discountPercent != null && (discountPercent < 0 || discountPercent > 100)) return false;
        return true;
    }

    public boolean hasValidInventory() {
        if (quantity == null || quantity < 0) return false;
        if (minimumStockLevel != null && minimumStockLevel < 0) return false;
        if (maximumStockLevel != null && maximumStockLevel < 0) return false;
        if (minimumStockLevel != null && maximumStockLevel != null) {
            if (minimumStockLevel > maximumStockLevel) return false;
        }
        return true;
    }

    public boolean hasValidWeight() {
        return weight == null || !(weight < 0);
    }

    public boolean hasValidDimensions() {
        if (dimensions != null && !dimensions.isBlank()) {
            return dimensions.matches("^\\d+(\\.\\d+)?\\s*[a-zA-Z]+\\s*x\\s*\\d+(\\.\\d+)?\\s*[a-zA-Z]+\\s*x\\s*\\d+(\\.\\d+)?\\s*[a-zA-Z]+$");
        }
        return true;
    }

    public boolean hasValidReturnPeriod() {
        return returnPeriodDays == null || returnPeriodDays >= 0;
    }

    public boolean isValidPreorderDate() {
        if (preorderAllowed && preorderAvailableFrom == null) return false;
        if (preorderAvailableFrom != null && preorderAvailableFrom.isBefore(LocalDateTime.now())) return false;
        return true;
    }
}
