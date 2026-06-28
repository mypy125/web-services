package com.mygitgor.seller_service.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Category response DTO represented as an immutable record")
public record CategoryResponse(
        @Schema(description = "Category ID", example = "cat-123e4567-e89b-12d3-a456-426614174000")
        String id,

        @Schema(description = "Category ID (external)", example = "CAT-001")
        String categoryId,

        @Schema(description = "Category name", example = "Electronics")
        String name,

        @Schema(description = "Category slug", example = "electronics")
        String slug,

        @Schema(description = "Category description", example = "All electronic devices and accessories")
        String description,

        @Schema(description = "Parent category")
        CategoryResponse parentCategory,

        @Schema(description = "Sub-categories")
        List<CategoryResponse> subCategories,

        @Schema(description = "Category level (0 = root)", example = "0")
        Integer level,

        @Schema(description = "Category path", example = "Electronics > Computers > Laptops")
        String path,

        @Schema(description = "Full category path", example = "Electronics > Computers > Laptops > Gaming Laptops")
        String fullPath,

        @Schema(description = "Category depth", example = "3")
        Integer depth,

        @Schema(description = "Sort order", example = "1")
        Integer sortOrder,

        @Schema(description = "Display order", example = "1")
        Integer displayOrder,

        @Schema(description = "Is active", example = "true")
        boolean isActive,

        @Schema(description = "Is visible", example = "true")
        boolean isVisible,

        @Schema(description = "Is featured", example = "true")
        boolean isFeatured,

        @Schema(description = "Is root category", example = "true")
        boolean isRoot,

        @Schema(description = "Has sub-categories", example = "true")
        boolean hasSubCategories,

        @Schema(description = "Category icon", example = "fa-laptop")
        String icon,

        @Schema(description = "Category image URL", example = "https://example.com/category.jpg")
        String imageUrl,

        @Schema(description = "Category banner URL", example = "https://example.com/banner.jpg")
        String bannerUrl,

        @Schema(description = "Meta title", example = "Electronics - Best Deals")
        String metaTitle,

        @Schema(description = "Meta description", example = "Shop the best electronics at great prices")
        String metaDescription,

        @Schema(description = "Meta keywords", example = "electronics, gadgets, devices")
        String metaKeywords,

        @Schema(description = "Number of products in category", example = "150")
        Long productCount,

        @Schema(description = "Number of sub-categories", example = "5")
        Integer subCategoryCount,

        @Schema(description = "Total products including sub-categories", example = "450")
        Long totalProductsCount,

        @Schema(description = "Category attributes")
        List<CategoryAttributeDto> attributes,

        @Schema(description = "Allowed product attributes")
        List<String> allowedAttributes,

        @Schema(description = "Required product attributes")
        List<String> requiredAttributes,

        @Schema(description = "Created at")
        LocalDateTime createdAt,

        @Schema(description = "Updated at")
        LocalDateTime updatedAt,

        @Schema(description = "Display name", example = "Electronics")
        String displayName,

        @Schema(description = "Full display name with path", example = "Home > Electronics > Computers")
        String fullDisplayName
) {

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Category attribute DTO")
    public record CategoryAttributeDto(
            @Schema(description = "Attribute name", example = "Brand")
            String name,

            @Schema(description = "Attribute type", example = "STRING")
            String type,

            @Schema(description = "Is required", example = "true")
            boolean required,

            @Schema(description = "Is filterable", example = "true")
            boolean filterable,

            @Schema(description = "Is searchable", example = "true")
            boolean searchable,

            @Schema(description = "Default values")
            List<String> defaultValues
    ) {}

    public boolean isRoot() {
        return level != null && level == 0;
    }

    public boolean hasChildren() {
        return subCategories != null && !subCategories.isEmpty();
    }

    @Override
    public String displayName() {
        if (displayName != null) return displayName;
        return name;
    }

    @Override
    public String fullDisplayName() {
        if (fullDisplayName != null) return fullDisplayName;
        if (path != null) return path;
        return name;
    }

    @Override
    public Long totalProductsCount() {
        if (totalProductsCount != null) return totalProductsCount;
        return productCount != null ? productCount : 0L;
    }

    public boolean isRootCategory() {
        return isRoot || (level != null && level == 0);
    }
}