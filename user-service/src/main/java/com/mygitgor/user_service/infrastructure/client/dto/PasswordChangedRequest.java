package com.mygitgor.user_service.infrastructure.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data transfer object representing a request sent to notify a user that their password was successfully changed")
public record PasswordChangedRequest(

        @Schema(description = "The electronic mail address of the recipient", example = "alex.patterson@example.com")
        @NotBlank(message = "Recipient email cannot be blank")
        @Email(message = "Invalid email format")
        String to
) {}
