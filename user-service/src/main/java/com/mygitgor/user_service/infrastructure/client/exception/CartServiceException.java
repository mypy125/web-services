package com.mygitgor.user_service.infrastructure.client.exception;

public class CartServiceException extends ServiceClientException {

    public CartServiceException(String operation, String message) {
        super("Cart Service", operation, message);
    }

    public CartServiceException(String operation, int statusCode, String message) {
        super("Cart Service", operation, statusCode, message);
    }

    public CartServiceException(String operation, int statusCode, String message, Throwable cause) {
        super("Cart Service", operation, statusCode, message, cause);
    }

    public static CartServiceException cartNotFound(String userId) {
        return new CartServiceException("FIND_CART", 404,
                "Cart not found for user: " + userId);
    }

    public static CartServiceException itemNotFound(String userId, String productId) {
        return new CartServiceException("FIND_ITEM", 404,
                "Item not found in cart for user: " + userId + ", product: " + productId);
    }

    public static CartServiceException invalidRequest(String identifier, String errorBody) {
        return new CartServiceException("VALIDATE", 400,
                "Invalid cart request: " + identifier + " - " + errorBody);
    }

    public static CartServiceException accessDenied(String identifier) {
        return new CartServiceException("ACCESS_DENIED", 403,
                "Access denied to cart: " + identifier);
    }

    public static CartServiceException conflict(String identifier, String errorBody) {
        return new CartServiceException("CONFLICT", 409,
                "Cart conflict: " + identifier + " - " + errorBody);
    }

    public static CartServiceException productOutOfStock(String productId) {
        return new CartServiceException("ADD_ITEM", 400,
                "Product out of stock: " + productId);
    }

    public static CartServiceException maxQuantityExceeded(String productId, int maxQuantity) {
        return new CartServiceException("ADD_ITEM", 400,
                "Maximum quantity exceeded for product: " + productId + ", max: " + maxQuantity);
    }

    public static CartServiceException minQuantityRequired(String productId, int minQuantity) {
        return new CartServiceException("UPDATE_QUANTITY", 400,
                "Minimum quantity required for product: " + productId + ", min: " + minQuantity);
    }

    public static CartServiceException invalidQuantity(String productId, Integer quantity) {
        return new CartServiceException("UPDATE_QUANTITY", 400,
                "Invalid quantity for product: " + productId + ", quantity: " + quantity);
    }

    public static CartServiceException cartIsEmpty(String userId) {
        return new CartServiceException("CLEAR_CART", 400,
                "Cart is already empty for user: " + userId);
    }

    public static CartServiceException cartInactive(String userId) {
        return new CartServiceException("CART_STATUS", 400,
                "Cart is inactive for user: " + userId);
    }

    public static CartServiceException addItemFailed(String userId, String productId, String errorBody) {
        return new CartServiceException("ADD_ITEM", 500,
                "Failed to add item to cart for user: " + userId + ", product: " + productId +
                        " - " + errorBody);
    }

    public static CartServiceException removeItemFailed(String userId, String productId, String errorBody) {
        return new CartServiceException("REMOVE_ITEM", 500,
                "Failed to remove item from cart for user: " + userId + ", product: " + productId +
                        " - " + errorBody);
    }

    public static CartServiceException updateQuantityFailed(String userId, String productId, String errorBody) {
        return new CartServiceException("UPDATE_QUANTITY", 500,
                "Failed to update item quantity for user: " + userId + ", product: " + productId +
                        " - " + errorBody);
    }

    public static CartServiceException clearCartFailed(String userId, String errorBody) {
        return new CartServiceException("CLEAR_CART", 500,
                "Failed to clear cart for user: " + userId + " - " + errorBody);
    }

    public static CartServiceException serviceError(String operation, String message) {
        return new CartServiceException(operation, 500,
                "Cart service error: " + message);
    }

    public static CartServiceException timeout(String operation) {
        return new CartServiceException(operation, 408,
                "Cart service timeout for operation: " + operation);
    }
}
