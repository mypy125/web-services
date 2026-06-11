package com.mygitgor.user_service.infrastructure.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLastLoginRequest {
    @NotNull(message = "Last login time is required")
    private LocalDateTime lastLoginAt;
}
