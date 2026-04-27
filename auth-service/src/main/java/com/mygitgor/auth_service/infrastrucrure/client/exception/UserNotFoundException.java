package com.mygitgor.auth_service.infrastrucrure.client.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
