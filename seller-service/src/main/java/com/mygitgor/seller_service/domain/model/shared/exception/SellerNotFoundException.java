package com.mygitgor.seller_service.domain.model.shared.exception;

public class SellerNotFoundException extends RuntimeException{
    public SellerNotFoundException(String message) {
        super(message);
    }
}