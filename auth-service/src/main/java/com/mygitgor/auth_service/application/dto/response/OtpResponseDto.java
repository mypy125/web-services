package com.mygitgor.auth_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OTP response (for development only)")
public class OtpResponseDto {

    @Schema(description = "OTP code (only in development mode)", example = "123456")
    private String otp;

    @Schema(description = "Email where OTP was sent", example = "user@example.com")
    private String email;

    @Schema(description = "OTP purpose", example = "LOGIN")
    private String purpose;

    @Schema(description = "OTP expiration time")
    private LocalDateTime expiresAt;

    @Schema(description = "Message", example = "OTP sent successfully")
    private String message;
}
