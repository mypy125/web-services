package com.mygitgor.seller_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

@Builder
@Schema(description = "Category list response represented as an immutable record")
public record CategoryListResponse(
        @Schema(description = "List of categories")
        List<CategoryResponse> categories,

        @Schema(description = "Total categories", example = "25")
        Long total,

        @Schema(description = "Current page", example = "0")
        Integer page,

        @Schema(description = "Page size", example = "20")
        Integer size,

        @Schema(description = "Total pages", example = "2")
        Integer totalPages,

        @Schema(description = "Is last page", example = "false")
        boolean last,

        @Schema(description = "Is first page", example = "true")
        boolean first,

        @Schema(description = "Number of elements", example = "20")
        Integer numberOfElements,

        @Schema(description = "Is empty", example = "false")
        boolean empty
) {}