package com.mygitgor.seller_service.infrastructure.client.exception;

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

    public static OrderServiceException orderNotFound(String orderId) {
        return new OrderServiceException("FIND_ORDER", 404,
                "Order not found: " + orderId);
    }

    public static OrderServiceException orderNotFoundForSeller(String orderId, String sellerId) {
        return new OrderServiceException("FIND_ORDER", 404,
                "Order not found for seller: " + sellerId + ", order: " + orderId);
    }

    public static OrderServiceException orderNotFoundByNumber(String orderNumber) {
        return new OrderServiceException("FIND_ORDER_BY_NUMBER", 404,
                "Order not found by number: " + orderNumber);
    }

    public static OrderServiceException invalidRequest(String identifier, String errorBody) {
        return new OrderServiceException("VALIDATE", 400,
                "Invalid order request: " + identifier + " - " + errorBody);
    }

    public static OrderServiceException accessDenied(String identifier) {
        return new OrderServiceException("ACCESS_DENIED", 403,
                "Access denied to order: " + identifier);
    }

    public static OrderServiceException conflict(String identifier, String errorBody) {
        return new OrderServiceException("CONFLICT", 409,
                "Order conflict: " + identifier + " - " + errorBody);
    }

    public static OrderServiceException cannotCancel(String orderId, String status) {
        return new OrderServiceException("CANCEL_ORDER", 400,
                "Cannot cancel order: " + orderId + " with status: " + status);
    }

    public static OrderServiceException cannotReturn(String orderId, String status) {
        return new OrderServiceException("RETURN_ORDER", 400,
                "Cannot return order: " + orderId + " with status: " + status);
    }

    public static OrderServiceException cannotReturnByStatus(String orderId, String status) {
        return new OrderServiceException("RETURN_ORDER", 400,
                "Cannot return order: " + orderId + " with status: " + status +
                        ". Order must be in DELIVERED status for return.");
    }

    public static OrderServiceException alreadyReturned(String orderId) {
        return new OrderServiceException("RETURN_ORDER", 400,
                "Order already returned: " + orderId);
    }

    public static OrderServiceException alreadyCancelled(String orderId) {
        return new OrderServiceException("CANCEL_ORDER", 400,
                "Order already cancelled: " + orderId);
    }

    public static OrderServiceException cannotConfirm(String orderId) {
        return new OrderServiceException("CONFIRM_ORDER", 400,
                "Cannot confirm order: " + orderId);
    }

    public static OrderServiceException notFoundForCancel(String orderId) {
        return new OrderServiceException("CANCEL_ORDER", 404,
                "Order not found for cancellation: " + orderId);
    }

    public static OrderServiceException createFailed(String sellerId, String errorBody) {
        return new OrderServiceException("CREATE_ORDER", 500,
                "Failed to create order for seller: " + sellerId + " - " + errorBody);
    }

    public static OrderServiceException updateFailed(String orderId, String errorBody) {
        return new OrderServiceException("UPDATE_ORDER", 500,
                "Failed to update order: " + orderId + " - " + errorBody);
    }

    public static OrderServiceException deleteFailed(String orderId, String errorBody) {
        return new OrderServiceException("DELETE_ORDER", 500,
                "Failed to delete order: " + orderId + " - " + errorBody);
    }

    public static OrderServiceException getOrdersFailed(String sellerId, String errorBody) {
        return new OrderServiceException("GET_ORDERS", 500,
                "Failed to get orders for seller: " + sellerId + " - " + errorBody);
    }

    public static OrderServiceException getStatisticsFailed(String sellerId) {
        return new OrderServiceException("GET_STATISTICS", 500,
                "Failed to get order statistics for seller: " + sellerId);
    }

    public static OrderServiceException getDetailsFailed(String orderId) {
        return new OrderServiceException("GET_DETAILS", 500,
                "Failed to get order details: " + orderId);
    }

    public static OrderServiceException cancelFailed(String orderId, String errorBody) {
        return new OrderServiceException("CANCEL_ORDER", 500,
                "Failed to cancel order: " + orderId + " - " + errorBody);
    }

    public static OrderServiceException returnFailed(String orderId, String errorBody) {
        return new OrderServiceException("RETURN_ORDER", 500,
                "Failed to return order: " + orderId + " - " + errorBody);
    }

    public static OrderServiceException confirmFailed(String orderId, String errorBody) {
        return new OrderServiceException("CONFIRM_ORDER", 500,
                "Failed to confirm order: " + orderId + " - " + errorBody);
    }

    public static OrderServiceException statusUpdateFailed(String orderId, String status, String errorBody) {
        return new OrderServiceException("UPDATE_STATUS", 500,
                "Failed to update order status: " + orderId + " to " + status + " - " + errorBody);
    }

    public static OrderServiceException deliveryStatusUpdateFailed(String orderId, String status, String errorBody) {
        return new OrderServiceException("UPDATE_DELIVERY", 500,
                "Failed to update delivery status: " + orderId + " to " + status + " - " + errorBody);
    }

    public static OrderServiceException serviceError(String operation, String message) {
        return new OrderServiceException(operation, 500,
                "Order service error: " + message);
    }

    public static OrderServiceException timeout(String operation) {
        return new OrderServiceException(operation, 408,
                "Order service timeout for operation: " + operation);
    }

    public static OrderServiceException unavailable(String operation) {
        return new OrderServiceException(operation, 503,
                "Order service unavailable for operation: " + operation);
    }
}
