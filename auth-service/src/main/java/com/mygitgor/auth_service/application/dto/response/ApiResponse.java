package com.mygitgor.auth_service.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class ApiResponse {

    @Schema(description = "Success flag", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response data")
    private Object data;

    @Schema(description = "Error details")
    private ErrorDetails error;

    @Schema(description = "Response timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Request ID for tracking", example = "550e8400-e29b-41d4-a716-446655440000")
    private String requestId;

    @Schema(description = "API version", example = "v1")
    private String version;

    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse error(String message, String errorCode) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .error(ErrorDetails.builder().code(errorCode).build())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Error details")
    public static class ErrorDetails {
        @Schema(description = "Error code", example = "AUTH_001")
        private String code;

        @Schema(description = "Error description", example = "Invalid OTP")
        private String description;

        @Schema(description = "Validation errors")
        private Map<String, List<String>> validationErrors;

        @Schema(description = "Error path", example = "/auth/login")
        private String path;
    }
}
