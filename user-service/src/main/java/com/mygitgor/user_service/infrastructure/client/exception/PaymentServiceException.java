package com.mygitgor.user_service.infrastructure.client.exception;

public class PaymentServiceException extends ServiceClientException {

    public PaymentServiceException(String operation, String message) {
        super("Payment Service", operation, message);
    }

    public PaymentServiceException(String operation, int statusCode, String message) {
        super("Payment Service", operation, statusCode, message);
    }

    public PaymentServiceException(String operation, int statusCode, String message, Throwable cause) {
        super("Payment Service", operation, statusCode, message, cause);
    }

    public static PaymentServiceException notFound(String identifier) {
        return new PaymentServiceException("FIND_PAYMENT_METHOD", 404,
                "Payment method not found: " + identifier);
    }

    public static PaymentServiceException notFoundForUser(String userId, String paymentMethodId) {
        return new PaymentServiceException("FIND_PAYMENT_METHOD", 404,
                "Payment method not found for user: " + userId + ", method: " + paymentMethodId);
    }

    public static PaymentServiceException defaultMethodNotFound(String userId) {
        return new PaymentServiceException("GET_DEFAULT", 404,
                "Default payment method not found for user: " + userId);
    }

    public static PaymentServiceException noPaymentMethods(String userId) {
        return new PaymentServiceException("GET_USER_METHODS", 404,
                "No payment methods found for user: " + userId);
    }

    public static PaymentServiceException invalidRequest(String identifier, String errorBody) {
        return new PaymentServiceException("VALIDATE", 400,
                "Invalid payment request: " + identifier + " - " + errorBody);
    }

    public static PaymentServiceException accessDenied(String identifier) {
        return new PaymentServiceException("ACCESS_DENIED", 403,
                "Access denied to payment method: " + identifier);
    }

    public static PaymentServiceException conflict(String identifier, String errorBody) {
        return new PaymentServiceException("CONFLICT", 409,
                "Payment method conflict: " + identifier + " - " + errorBody);
    }

    public static PaymentServiceException addFailed(String userId, String errorBody) {
        return new PaymentServiceException("ADD_METHOD", 400,
                "Failed to add payment method for user: " + userId + " - " + errorBody);
    }

    public static PaymentServiceException updateDefaultFailed(String userId, String paymentMethodId) {
        return new PaymentServiceException("UPDATE_DEFAULT", 400,
                "Failed to update default payment method for user: " + userId +
                        ", method: " + paymentMethodId);
    }

    public static PaymentServiceException deleteFailed(String userId, String paymentMethodId) {
        return new PaymentServiceException("DELETE_METHOD", 400,
                "Failed to delete payment method for user: " + userId +
                        ", method: " + paymentMethodId);
    }

    public static PaymentServiceException cannotDeleteLastMethod(String userId) {
        return new PaymentServiceException("DELETE_METHOD", 400,
                "Cannot delete the last payment method for user: " + userId);
    }

    public static PaymentServiceException cannotDeleteDefaultMethod(String userId, String paymentMethodId) {
        return new PaymentServiceException("DELETE_METHOD", 400,
                "Cannot delete default payment method for user: " + userId +
                        ", method: " + paymentMethodId);
    }

    public static PaymentServiceException invalidPaymentType(String type) {
        return new PaymentServiceException("VALIDATE", 400,
                "Invalid payment method type: " + type);
    }

    public static PaymentServiceException invalidCardData(String errorBody) {
        return new PaymentServiceException("VALIDATE_CARD", 400,
                "Invalid card data: " + errorBody);
    }

    public static PaymentServiceException expiredCard(String cardLast4) {
        return new PaymentServiceException("VALIDATE_CARD", 400,
                "Card expired: ****" + cardLast4);
    }

    public static PaymentServiceException insufficientFunds(String paymentMethodId) {
        return new PaymentServiceException("PAYMENT", 402,
                "Insufficient funds for payment method: " + paymentMethodId);
    }

    public static PaymentServiceException processingError(String paymentMethodId, String errorBody) {
        return new PaymentServiceException("PAYMENT", 500,
                "Payment processing error: " + paymentMethodId + " - " + errorBody);
    }

    public static PaymentServiceException serviceError(String operation, String message) {
        return new PaymentServiceException(operation, 500,
                "Payment service error: " + message);
    }

    public static PaymentServiceException timeout(String operation) {
        return new PaymentServiceException(operation, 408,
                "Payment service timeout for operation: " + operation);
    }
}
