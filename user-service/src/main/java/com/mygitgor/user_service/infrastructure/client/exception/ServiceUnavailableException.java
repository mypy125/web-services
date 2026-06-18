package com.mygitgor.user_service.infrastructure.client.exception;

public class ServiceUnavailableException extends ServiceClientException {

    public ServiceUnavailableException(String serviceName) {
        super(serviceName, "UNKNOWN", "Service is unavailable");
    }

    public ServiceUnavailableException(String serviceName, String operation) {
        super(serviceName, operation, "Service is unavailable for operation: " + operation);
    }

    public ServiceUnavailableException(String serviceName, String operation, Throwable cause) {
        super(serviceName, operation, 503, "Service unavailable", cause);
    }
}
