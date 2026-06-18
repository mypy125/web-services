package com.mygitgor.user_service.infrastructure.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data transfer object representing a request to send a welcome email to a newly registered user")
public record WelcomeEmailRequest(

        @Schema(description = "Recipient electronic mail address", example = "welcome.user@example.com")
        @NotBlank(message = "Recipient email cannot be blank")
        @Email(message = "Invalid email format")
        String to,

        @Schema(description = "Display name of the user to personalize the email template", example = "Alexander")
        @NotBlank(message = "Recipient name cannot be blank")
        String name
) {}
