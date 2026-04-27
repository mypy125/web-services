package com.mygitgor.auth_service.infrastrucrure.client.exception;

public class SellerNotFoundException extends RuntimeException {
    public SellerNotFoundException(String message) {
        super(message);
    }
}

