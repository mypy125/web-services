package com.mygitgor.user_service.infrastructure.shared.exception;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
