package com.mygitgor.seller_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import java.util.List;

@Builder
@Schema(description = "Category request DTO represented as an immutable record")
public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Schema(description = "Category name", example = "Electronics")
        String name,

        @Schema(description = "Category description", example = "All electronic devices")
        String description,

        @Schema(description = "Parent category ID", example = "cat-123e4567-e89b-12d3-a456-426614174000")
        String parentCategoryId,

        @Schema(description = "Category ID (external)", example = "CAT-001")
        String categoryId,

        @Schema(description = "Category icon", example = "fa-laptop")
        String icon,

        @Schema(description = "Category image URL", example = "https://example.com/category.jpg")
        String imageUrl,

        @Schema(description = "Sort order", example = "1")
        Integer sortOrder,

        @Schema(description = "Display order", example = "1")
        Integer displayOrder,

        @Schema(description = "Is active", example = "true")
        Boolean isActive,

        @Schema(description = "Is visible", example = "true")
        Boolean isVisible,

        @Schema(description = "Is featured", example = "true")
        Boolean isFeatured,

        @Schema(description = "Meta title", example = "Electronics - Best Deals")
        String metaTitle,

        @Schema(description = "Meta description", example = "Shop the best electronics")
        String metaDescription,

        @Schema(description = "Meta keywords", example = "electronics, gadgets")
        String metaKeywords,

        @Schema(description = "Category attributes")
        List<CategoryAttributeDto> attributes,

        @Schema(description = "Allowed product attributes")
        List<String> allowedAttributes,

        @Schema(description = "Required product attributes")
        List<String> requiredAttributes
) {

    @Builder
    @Schema(description = "Category attribute DTO represented as an immutable record")
    public record CategoryAttributeDto(
            @Schema(description = "Attribute name", example = "Brand")
            String name,

            @Schema(description = "Attribute type", example = "STRING")
            String type,

            @Schema(description = "Is required", example = "true")
            Boolean required,

            @Schema(description = "Is filterable", example = "true")
            Boolean filterable,

            @Schema(description = "Is searchable", example = "true")
            Boolean searchable,

            @Schema(description = "Default values")
            List<String> defaultValues
    ) {}
}