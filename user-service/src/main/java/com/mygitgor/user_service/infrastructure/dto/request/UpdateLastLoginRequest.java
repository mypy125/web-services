package com.mygitgor.user_service.infrastructure.dto.request;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Request package to log the user's successful authentication timestamp")
public record UpdateLastLoginRequest(

        @Schema(description = "The exact date and time of the successful login event", example = "2026-06-15T17:45:00")
        @NotNull(message = "Last login time is required")
        LocalDateTime lastLoginAt
) {}
