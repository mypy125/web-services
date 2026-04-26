package com.mygitgor.auth_service.domain.shared.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class TokenValue {
    private final String value;

    public TokenValue(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("Token cannot be null or empty");
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
