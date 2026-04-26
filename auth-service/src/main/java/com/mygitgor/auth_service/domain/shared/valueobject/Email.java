package com.mygitgor.auth_service.domain.shared.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public class Email {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final String value;

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
