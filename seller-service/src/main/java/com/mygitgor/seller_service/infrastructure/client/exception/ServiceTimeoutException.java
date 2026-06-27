package com.mygitgor.seller_service.infrastructure.client.exception;

public class ServiceTimeoutException extends ServiceClientException {

    public ServiceTimeoutException(String serviceName) {
        super(serviceName, "TIMEOUT", "Request timeout for service: " + serviceName);
    }

    public ServiceTimeoutException(String serviceName, String operation, long timeoutMs) {
        super(serviceName, operation, String.format("Request timeout after %d ms for operation: %s", timeoutMs, operation));
    }

    public ServiceTimeoutException(String serviceName, String operation, long timeoutMs, Throwable cause) {
        super(serviceName, operation, 408, String.format("Request timeout after %d ms", timeoutMs), cause);
    }
}
