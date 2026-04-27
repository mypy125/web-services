package com.mygitgor.auth_service.infrastrucrure.client.exception;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(String message) {
        super(message);
    }

}
