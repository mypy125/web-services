package com.mygitgor.seller_service.infrastructure.client.exception;

public class CouponServiceException extends ServiceClientException {

    public CouponServiceException(String operation, String message) {
        super("Coupon Service", operation, message);
    }

    public CouponServiceException(String operation, int statusCode, String message) {
        super("Coupon Service", operation, statusCode, message);
    }

    public CouponServiceException(String operation, int statusCode, String message, Throwable cause) {
        super("Coupon Service", operation, statusCode, message, cause);
    }

    public static CouponServiceException couponNotFound(String couponId) {
        return new CouponServiceException("GET_COUPON", 404,
                "Coupon not found with identifier: " + couponId);
    }

    public static CouponServiceException invalidCouponRequest(String identifier, String errorBody) {
        return new CouponServiceException("VALIDATE_COUPON", 400,
                "Invalid coupon request data: " + identifier + " - " + errorBody);
    }

    public static CouponServiceException accessDenied(String couponId) {
        return new CouponServiceException("ACCESS_DENIED", 403,
                "Access denied to coupon: " + couponId);
    }

    public static CouponServiceException couponConflict(String code, String errorBody) {
        return new CouponServiceException("CREATE_COUPON", 409,
                "Coupon code conflict [" + code + "]: " + errorBody);
    }

    public static CouponServiceException createFailed(String sellerId, String errorBody) {
        return new CouponServiceException("CREATE_COUPON", 500,
                "Failed to create coupon for seller: " + sellerId + " - " + errorBody);
    }

    public static CouponServiceException getCouponsFailed(String sellerId, String errorBody) {
        return new CouponServiceException("GET_COUPONS", 500,
                "Failed to retrieve coupons list for seller: " + sellerId + " - " + errorBody);
    }

    public static CouponServiceException serviceError(String operation, String message) {
        return new CouponServiceException(operation, 500,
                "Coupon service internal error: " + message);
    }

    public static CouponServiceException timeout(String operation) {
        return new CouponServiceException(operation, 408,
                "Coupon service timeout for operation: " + operation);
    }

    public static CouponServiceException unavailable(String operation) {
        return new CouponServiceException(operation, 503,
                "Coupon service unavailable for operation: " + operation);
    }
}
