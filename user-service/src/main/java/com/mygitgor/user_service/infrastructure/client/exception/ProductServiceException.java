package com.mygitgor.user_service.infrastructure.client.exception;

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
        return new ProductServiceException("FIND_PRODUCT", 404, "Product not found: " + productId);
    }

    public static ProductServiceException outOfStock(String productId) {
        return new ProductServiceException("CHECK_STOCK", 400, "Product out of stock: " + productId);
    }
}
