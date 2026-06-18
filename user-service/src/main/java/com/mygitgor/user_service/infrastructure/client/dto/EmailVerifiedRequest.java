package com.mygitgor.user_service.infrastructure.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data transfer object representing a request sent after a user successfully verifies their email address")
public record EmailVerifiedRequest(

        @Schema(description = "The verified electronic mail address of the recipient", example = "alex.patterson@example.com")
        @NotBlank(message = "Recipient email cannot be blank")
        @Email(message = "Invalid email format")
        String to
) {}
