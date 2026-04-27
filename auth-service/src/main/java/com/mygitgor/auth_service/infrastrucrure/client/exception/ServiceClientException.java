package com.mygitgor.auth_service.infrastrucrure.client.exception;

public class ServiceClientException extends RuntimeException {
    public ServiceClientException(String message) {
        super(message);
    }

    public ServiceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
