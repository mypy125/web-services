package com.mygitgor.seller_service.infrastructure.client.exception;

public class ProductServiceException extends ServiceClientException {

    public ProductServiceException(String operation, String message) {
        super("Product Service", operation, message);
    }

    public ProductServiceException(String operation, int statusCode, String message) {
        super("Product Service", operation, statusCode, message);
    }

    public ProductServiceException(String operation, int statusCode, String message, Throwable cause) {
        super("Product Service", operation, statusCode, message, cause);
    }

    public static ProductServiceException notFound(String productId) {
        return new ProductServiceException("FIND_PRODUCT", 404,
                "Product not found: " + productId);
    }

    public static ProductServiceException notFoundForSeller(String productId, String sellerId) {
        return new ProductServiceException("FIND_PRODUCT", 404,
                "Product not found for seller: " + sellerId + ", product: " + productId);
    }

    public static ProductServiceException invalidRequest(String identifier, String errorBody) {
        return new ProductServiceException("VALIDATE", 400,
                "Invalid product request: " + identifier + " - " + errorBody);
    }

    public static ProductServiceException accessDenied(String identifier) {
        return new ProductServiceException("ACCESS_DENIED", 403,
                "Access denied to product: " + identifier);
    }

    public static ProductServiceException conflict(String identifier, String errorBody) {
        return new ProductServiceException("CONFLICT", 409,
                "Product conflict: " + identifier + " - " + errorBody);
    }

    public static ProductServiceException productInactive(String productId) {
        return new ProductServiceException("PRODUCT_STATUS", 400,
                "Product is inactive: " + productId);
    }

    public static ProductServiceException outOfStock(String productId) {
        return new ProductServiceException("INVENTORY", 400,
                "Product out of stock: " + productId);
    }

    public static ProductServiceException insufficientStock(String productId, int requested, int available) {
        return new ProductServiceException("INVENTORY", 400,
                "Insufficient stock for product: " + productId +
                        ", requested: " + requested + ", available: " + available);
    }

    public static ProductServiceException notBelongToSeller(String productId, String sellerId) {
        return new ProductServiceException("AUTHORIZATION", 403,
                "Product does not belong to seller: " + sellerId + ", product: " + productId);
    }

    public static ProductServiceException createFailed(String sellerId, String errorBody) {
        return new ProductServiceException("CREATE_PRODUCT", 500,
                "Failed to create product for seller: " + sellerId + " - " + errorBody);
    }

    public static ProductServiceException updateFailed(String productId, String errorBody) {
        return new ProductServiceException("UPDATE_PRODUCT", 500,
                "Failed to update product: " + productId + " - " + errorBody);
    }

    public static ProductServiceException deleteFailed(String productId, String errorBody) {
        return new ProductServiceException("DELETE_PRODUCT", 500,
                "Failed to delete product: " + productId + " - " + errorBody);
    }

    public static ProductServiceException getProductsFailed(String sellerId, String errorBody) {
        return new ProductServiceException("GET_PRODUCTS", 500,
                "Failed to get products for seller: " + sellerId + " - " + errorBody);
    }

    public static ProductServiceException getStatisticsFailed(String sellerId) {
        return new ProductServiceException("GET_STATISTICS", 500,
                "Failed to get product statistics for seller: " + sellerId);
    }

    public static ProductServiceException categoryNotFound(String category) {
        return new ProductServiceException("FIND_CATEGORY", 404,
                "Category not found: " + category);
    }

    public static ProductServiceException invalidSku(String sku) {
        return new ProductServiceException("VALIDATE_SKU", 400,
                "Invalid SKU: " + sku);
    }

    public static ProductServiceException skuAlreadyExists(String sku) {
        return new ProductServiceException("VALIDATE_SKU", 409,
                "SKU already exists: " + sku);
    }

    public static ProductServiceException priceUpdateFailed(String productId, String errorBody) {
        return new ProductServiceException("UPDATE_PRICE", 400,
                "Failed to update price for product: " + productId + " - " + errorBody);
    }

    public static ProductServiceException quantityUpdateFailed(String productId, String errorBody) {
        return new ProductServiceException("UPDATE_QUANTITY", 400,
                "Failed to update quantity for product: " + productId + " - " + errorBody);
    }

    public static ProductServiceException serviceError(String operation, String message) {
        return new ProductServiceException(operation, 500,
                "Product service error: " + message);
    }

    public static ProductServiceException timeout(String operation) {
        return new ProductServiceException(operation, 408,
                "Product service timeout for operation: " + operation);
    }

    public static ProductServiceException unavailable(String operation) {
        return new ProductServiceException(
                operation,
                503,
                "Product Service is down or failing to respond"
        );
    }
}
