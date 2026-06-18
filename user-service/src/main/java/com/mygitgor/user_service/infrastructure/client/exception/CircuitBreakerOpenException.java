package com.mygitgor.user_service.infrastructure.client.exception;

public class CircuitBreakerOpenException extends ServiceClientException {

    public CircuitBreakerOpenException(String serviceName) {
        super(serviceName, "CIRCUIT_BREAKER_OPEN", "Circuit breaker is open for service: " + serviceName);
    }

    public CircuitBreakerOpenException(String serviceName, String operation) {
        super(serviceName, operation, "Circuit breaker is open for service: " + serviceName + " - " + operation);
    }
}
