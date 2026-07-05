package com.mygitgor.transaction_service.shared.exception;

public class DomainException extends RuntimeException{
    public DomainException(String message) {
        super(message);
    }
}
