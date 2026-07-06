package com.mygitgor.transaction_service.shared.valueobject;

import com.mygitgor.transaction_service.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class PayoutId {
    private final UUID value;

    public PayoutId() {
        this.value = UUID.randomUUID();
    }

    public PayoutId(String value) {
        try {
            this.value = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid user ID format");
        }
    }

    public PayoutId(UUID value) {
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
