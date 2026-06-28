package com.mygitgor.seller_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

@Builder
@Schema(description = "Category hierarchy response represented as an immutable record")
public record CategoryHierarchyResponse(
        @Schema(description = "Root categories")
        List<CategoryNode> rootCategories,

        @Schema(description = "Total categories", example = "25")
        Long total,

        @Schema(description = "Max depth", example = "4")
        Integer maxDepth
) {

    @Builder
    @Schema(description = "Category node")
    public record CategoryNode(
            CategoryResponse category,
            List<CategoryNode> children,
            Integer depth,
            boolean hasChildren
    ) {}
}