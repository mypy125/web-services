package com.mygitgor.auth_service.application.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination response wrapper")
public class PageResponseDto<T> {

    @Schema(description = "List of items")
    private List<T> content;

    @Schema(description = "Current page number", example = "0")
    private int pageNumber;

    @Schema(description = "Items per page", example = "20")
    private int pageSize;

    @Schema(description = "Total number of elements", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Is last page", example = "false")
    private boolean last;

    @Schema(description = "Is first page", example = "true")
    private boolean first;

    @Schema(description = "Number of items in current page", example = "20")
    private int numberOfElements;

    @Schema(description = "Is page empty", example = "false")
    private boolean empty;
}
