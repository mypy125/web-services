package com.mygitgor.seller_service.shared.exception;

public class SellerNotFoundException extends RuntimeException{
    public SellerNotFoundException(String message) {
        super(message);
    }
}