package com.mygitgor.auth_service.infrastrucrure.client.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
