package com.mygitgor.user_service.infrastructure.kafka;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String USER_CREATED_TOPIC = "user.created";
    public static final String USER_UPDATED_TOPIC = "user.updated";
    public static final String USER_DELETED_TOPIC = "user.deleted";
    public static final String EMAIL_VERIFIED_TOPIC = "user.email.verified";
    public static final String PASSWORD_CHANGED_TOPIC = "user.password.changed";
    public static final String USER_STATUS_CHANGED_TOPIC = "user.status.changed";
    public static final String USER_ROLE_CHANGED_TOPIC = "user.role.changed";
    public static final String USER_ORDER_STATS_UPDATED_TOPIC = "user.order.stats.updated";
    public static final String USER_LOYALTY_UPDATED_TOPIC = "user.loyalty.updated";
    public static final String USER_REVIEW_UPDATED_TOPIC = "user.review.updated";
    public static final String USER_ACTIVITY_UPDATED_TOPIC = "user.activity.updated";
    public static final String USER_COUPON_UPDATED_TOPIC= "user.coupon.updated";
    public static final String USER_PRODUCT_UPDATED_TOPIC = "user.product.updated";
    public static final String USER_WISHLIST_UPDATED_TOPIC = "user.wishlist.updated";
    public static final String USER_CART_UPDATED_TOPIC = "user.cart.updated";
}
