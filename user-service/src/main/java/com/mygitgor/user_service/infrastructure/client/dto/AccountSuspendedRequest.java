package com.mygitgor.user_service.infrastructure.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data transfer object representing a request to send an account suspension notification email")
public record AccountSuspendedRequest(

        @Schema(description = "The electronic mail address of the suspended user", example = "user@example.com")
        @NotBlank(message = "Recipient email cannot be blank")
        @Email(message = "Invalid email format")
        String to,

        @Schema(description = "The reason or duration details for the temporary suspension", example = "Suspicious login attempts detected")
        @NotBlank(message = "Suspension reason cannot be blank")
        String reason
) {}
