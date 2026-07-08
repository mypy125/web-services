package com.mygitgor.seller_service.infrastructure.client.exception;

public class AuthServiceException extends ServiceClientException {

    public AuthServiceException(String operation, String message) {
        super("Auth Service", operation, message);
    }

    public AuthServiceException(String operation, int statusCode, String message) {
        super("Auth Service", operation, statusCode, message);
    }

    public AuthServiceException(String operation, int statusCode, String message, Throwable cause) {
        super("Auth Service", operation, statusCode, message, cause);
    }

    public static AuthServiceException invalidOtp(String email) {
        return new AuthServiceException("VERIFY_OTP", 400,
                "Invalid or expired OTP for email: " + email);
    }

    public static AuthServiceException invalidCredentials(String errorBody) {
        return new AuthServiceException("AUTHENTICATE", 401,
                "Authentication failed. Invalid credentials: " + errorBody);
    }

    public static AuthServiceException invalidToken() {
        return new AuthServiceException("VALIDATE_TOKEN", 401,
                "Provided token is expired, invalid or malformed");
    }

    public static AuthServiceException accessDenied() {
        return new AuthServiceException("ACCESS_DENIED", 403,
                "Access denied to the requested auth resource");
    }

    public static AuthServiceException userNotFound(String identifier) {
        return new AuthServiceException("GET_USER_INFO", 404,
                "User profile not found for identifier: " + identifier);
    }

    public static AuthServiceException verifyOtpFailed(String email, String errorBody) {
        return new AuthServiceException("VERIFY_OTP", 500,
                "Failed to verify OTP for email: " + email + " - " + errorBody);
    }

    public static AuthServiceException tokenValidationFailed(String errorBody) {
        return new AuthServiceException("VALIDATE_TOKEN", 500,
                "Critical failure during token validation on remote service: " + errorBody);
    }

    public static AuthServiceException getUserInfoFailed(String errorBody) {
        return new AuthServiceException("GET_USER_INFO", 500,
                "Failed to fetch user profile data from token: " + errorBody);
    }

    public static AuthServiceException serviceError(String operation, String message) {
        return new AuthServiceException(operation, 500,
                "Auth service internal error: " + message);
    }

    public static AuthServiceException timeout(String operation) {
        return new AuthServiceException(operation, 408,
                "Auth service timeout for operation: " + operation);
    }

    public static AuthServiceException unavailable(String operation) {
        return new AuthServiceException(operation, 503,
                "Auth service unavailable for operation: " + operation);
    }
}