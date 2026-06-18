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

    public static PaymentServiceException notFound(String paymentId) {
        return new PaymentServiceException("FIND_PAYMENT", 404, "Payment not found: " + paymentId);
    }

    public static PaymentServiceException methodNotFound(String userId) {
        return new PaymentServiceException("GET_DEFAULT", 404,
                "Default payment method not found for user: " + userId);
    }
}
