package com.mygitgor.user_service.infrastructure.shared.valueobject;

import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;

import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new DomainException("Invalid email format");
        }
        this.value = value.toLowerCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
