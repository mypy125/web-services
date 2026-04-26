package com.mygitgor.auth_service.domain.shared.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class UserId {
    private final UUID value;

    public UserId() {
        this.value = UUID.randomUUID();
    }

    public UserId(String value) {
        try {
            this.value = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid user ID format");
        }
    }

    public UserId(UUID value) {
        if (value == null) {
            throw new DomainException("User ID cannot be null");
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
