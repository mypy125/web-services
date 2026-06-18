package com.mygitgor.user_service.infrastructure.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data transfer object representing a request to send an account ban notification email")
public record AccountBannedRequest(

        @Schema(description = "The electronic mail address of the banned user", example = "blocked.user@example.com")
        @NotBlank(message = "Recipient email cannot be blank")
        @Email(message = "Invalid email format")
        String to,

        @Schema(description = "The official reason or policy violation description for the ban", example = "Terms of Service violation (Section 4.2)")
        @NotBlank(message = "Ban reason cannot be blank")
        String reason
) {}
