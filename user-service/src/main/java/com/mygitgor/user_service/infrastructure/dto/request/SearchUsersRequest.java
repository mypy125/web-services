package com.mygitgor.user_service.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Query parameters package for searching and filtering users with pagination")
public record SearchUsersRequest(
        @Schema(description = "Search term to filter by name or email", example = "john")
        String searchTerm,

        @Schema(description = "Zero-based page index", example = "0")
        @Min(value = 0, message = "Page index cannot be negative")
        Integer page,

        @Schema(description = "The size of the page to be returned", example = "20")
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer size,

        @Schema(description = "Filter by user security role", example = "ROLE_CUSTOMER")
        String role,

        @Schema(description = "Filter by account status", example = "ACTIVE")
        String accountStatus,

        @Schema(description = "Filter by email verification status", example = "true")
        Boolean emailVerified,

        @Schema(description = "Field name to sort the results by", example = "createdAt")
        String sortBy,

        @Schema(description = "Sort direction: ASC or DESC", example = "DESC")
        String sortDirection
) {
    public SearchUsersRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sortBy == null) sortBy = "createdAt";
        if (sortDirection == null) sortDirection = "DESC";
    }
}
