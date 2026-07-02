package com.mygitgor.user_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Paginated response wrapper containing a list of user profiles and pagination metadata")
public record UserListResponse(

        @Schema(description = "List of user payload items for the current page")
        @NotNull(message = "Users list cannot be null")
        List<UserResponse> users,

        @Schema(description = "Current page index (zero-based)", example = "0")
        int page,

        @Schema(description = "Number of elements requested per page", example = "20")
        int size,

        @Schema(description = "Total number of elements matching the search criteria across all pages", example = "1250")
        long totalElements,

        @Schema(description = "Total number of pages calculated from total elements and page size", example = "63")
        int totalPages,

        @Schema(description = "Flag indicating if the current page is the final one in the result set", example = "false")
        boolean last,

        @Schema(description = "Flag indicating if the current page is the first one in the result set", example = "true")
        boolean first
) {
    public UserListResponse {
        if (users == null) {
            users = List.of();
        } else {
            users = List.copyOf(users);
        }
    }
}
