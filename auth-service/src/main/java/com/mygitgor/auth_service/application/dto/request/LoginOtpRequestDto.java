package com.mygitgor.auth_service.application.dto.request;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request OTP for login")
public class LoginOtpRequestDto {

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @NotNull(message = "Role is required")
    @Schema(description = "User role", example = "ROLE_CUSTOMER")
    private UserRole role;
}
