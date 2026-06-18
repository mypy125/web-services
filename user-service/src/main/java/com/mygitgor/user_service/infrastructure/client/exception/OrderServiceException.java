package com.mygitgor.user_service.infrastructure.client.exception;

public class OrderServiceException extends ServiceClientException {

    public OrderServiceException(String operation, String message) {
        super("Order Service", operation, message);
    }

    public OrderServiceException(String operation, int statusCode, String message) {
        super("Order Service", operation, statusCode, message);
    }

    public OrderServiceException(String operation, int statusCode, String message, Throwable cause) {
        super("Order Service", operation, statusCode, message, cause);
    }

    public static OrderServiceException notFound(String identifier) {
        return new OrderServiceException("FIND_ORDER", 404,
                "Order not found: " + identifier);
    }

    public static OrderServiceException userOrdersNotFound(String userId) {
        return new OrderServiceException("GET_USER_ORDERS", 404,
                "Orders not found for user: " + userId);
    }

    public static OrderServiceException statisticsNotFound(String userId) {
        return new OrderServiceException("GET_STATISTICS", 404,
                "Order statistics not found for user: " + userId);
    }

    public static OrderServiceException invalidRequest(String identifier, String errorBody) {
        return new OrderServiceException("VALIDATE", 400,
                "Invalid request for order: " + identifier + " - " + errorBody);
    }

    public static OrderServiceException accessDenied(String identifier) {
        return new OrderServiceException("ACCESS_DENIED", 403,
                "Access denied to orders: " + identifier);
    }

    public static OrderServiceException conflict(String identifier, String errorBody) {
        return new OrderServiceException("CONFLICT", 409,
                "Order conflict: " + identifier + " - " + errorBody);
    }

    public static OrderServiceException notFoundByNumber(String orderNumber) {
        return new OrderServiceException("FIND_ORDER_BY_NUMBER", 404,
                "Order not found by number: " + orderNumber);
    }

    public static OrderServiceException cannotCancel(String orderId, String status) {
        return new OrderServiceException("CANCEL_ORDER", 400,
                "Cannot cancel order: " + orderId + " with status: " + status);
    }

    public static OrderServiceException cannotReturn(String orderId, String status) {
        return new OrderServiceException("RETURN_ORDER", 400,
                "Cannot return order: " + orderId + " with status: " + status);
    }

    public static OrderServiceException serviceError(String operation, String message) {
        return new OrderServiceException(operation, 500,
                "Order service error: " + message);
    }

    public static OrderServiceException timeout(String operation) {
        return new OrderServiceException(operation, 408,
                "Order service timeout for operation: " + operation);
    }
}
