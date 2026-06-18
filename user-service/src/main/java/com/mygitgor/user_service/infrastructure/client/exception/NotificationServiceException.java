package com.mygitgor.user_service.infrastructure.client.exception;

public class NotificationServiceException extends ServiceClientException {

    public NotificationServiceException(String operation, String message) {
        super("Notification Service", operation, message);
    }

    public NotificationServiceException(String operation, int statusCode, String message) {
        super("Notification Service", operation, statusCode, message);
    }

    public NotificationServiceException(String operation, int statusCode, String message, Throwable cause) {
        super("Notification Service", operation, statusCode, message, cause);
    }

    public static NotificationServiceException sendFailed(String email, String operation) {
        return new NotificationServiceException(operation, 500,
                "Failed to send notification to: " + email);
    }

    public static NotificationServiceException templateNotFound(String templateName) {
        return new NotificationServiceException("SEND_EMAIL", 404,
                "Email template not found: " + templateName);
    }

    public static NotificationServiceException invalidRequest(String email, String errorBody) {
        return new NotificationServiceException("VALIDATE", 400,
                "Invalid notification request for: " + email + " - " + errorBody);
    }

    public static NotificationServiceException rateLimited(String email) {
        return new NotificationServiceException("SEND_EMAIL", 429,
                "Rate limit exceeded for email: " + email);
    }

    public static NotificationServiceException serviceUnavailable(String operation) {
        return new NotificationServiceException(operation, 503,
                "Notification service unavailable");
    }

    public static NotificationServiceException timeout(String operation) {
        return new NotificationServiceException(operation, 408,
                "Notification service timeout for operation: " + operation);
    }
}
