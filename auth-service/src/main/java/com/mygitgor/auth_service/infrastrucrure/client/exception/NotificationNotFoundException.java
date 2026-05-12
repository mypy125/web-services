package com.mygitgor.auth_service.infrastrucrure.client.exception;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
