package com.mygitgor.user_service.infrastructure.client.exception;

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

    public static CouponServiceException notFound(String couponCode) {
        return new CouponServiceException("FIND_COUPON", 404,
                "Coupon not found: " + couponCode);
    }

    public static CouponServiceException alreadyUsed(String couponCode) {
        return new CouponServiceException("MARK_USED", 409,
                "Coupon already used: " + couponCode);
    }

    public static CouponServiceException expired(String couponCode) {
        return new CouponServiceException("VALIDATE", 400,
                "Coupon expired: " + couponCode);
    }

    public static CouponServiceException invalid(String couponCode) {
        return new CouponServiceException("VALIDATE", 400,
                "Invalid coupon: " + couponCode);
    }

    public static CouponServiceException notApplicable(String couponCode, String userId) {
        return new CouponServiceException("VALIDATE", 400,
                "Coupon not applicable for user: " + userId + ", coupon: " + couponCode);
    }

    public static CouponServiceException minOrderNotMet(String couponCode, Double minOrder) {
        return new CouponServiceException("VALIDATE", 400,
                "Minimum order amount not met for coupon: " + couponCode + ", min: " + minOrder);
    }

    public static CouponServiceException userCouponsNotFound(String userId) {
        return new CouponServiceException("GET_USER_COUPONS", 404,
                "Coupons not found for user: " + userId);
    }
}
