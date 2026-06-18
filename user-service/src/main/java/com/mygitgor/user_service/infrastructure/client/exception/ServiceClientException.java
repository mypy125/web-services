package com.mygitgor.user_service.infrastructure.client.exception;

import lombok.Getter;

@Getter
public class ServiceClientException extends RuntimeException {

    private final String serviceName;
    private final String operation;
    private final int statusCode;
    private final String requestId;

    public ServiceClientException(String serviceName, String operation, String message) {
        super(String.format("[%s] %s: %s", serviceName, operation, message));
        this.serviceName = serviceName;
        this.operation = operation;
        this.statusCode = 0;
        this.requestId = null;
    }

    public ServiceClientException(String serviceName, String operation, int statusCode, String message) {
        super(String.format("[%s] %s - Status: %d - %s", serviceName, operation, statusCode, message));
        this.serviceName = serviceName;
        this.operation = operation;
        this.statusCode = statusCode;
        this.requestId = null;
    }

    public ServiceClientException(String serviceName, String operation, int statusCode, String message, Throwable cause) {
        super(String.format("[%s] %s - Status: %d - %s", serviceName, operation, statusCode, message), cause);
        this.serviceName = serviceName;
        this.operation = operation;
        this.statusCode = statusCode;
        this.requestId = null;
    }

    public ServiceClientException(String serviceName, String operation, int statusCode, String message, String requestId) {
        super(String.format("[%s] %s - Status: %d - RequestId: %s - %s",
                serviceName, operation, statusCode, requestId, message));
        this.serviceName = serviceName;
        this.operation = operation;
        this.statusCode = statusCode;
        this.requestId = requestId;
    }
}
