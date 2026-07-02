package com.mygitgor.seller_service.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

public record UpdateLastLoginRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Last login timestamp is required")
        @PastOrPresent(message = "Last login timestamp cannot be in the future")
        LocalDateTime lastLoginAt
) {}
