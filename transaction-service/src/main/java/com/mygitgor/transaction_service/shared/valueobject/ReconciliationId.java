package com.mygitgor.transaction_service.shared.valueobject;

import com.mygitgor.transaction_service.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class ReconciliationId {
    private final UUID value;

    public ReconciliationId() {
        this.value = UUID.randomUUID();
    }

    public ReconciliationId(String value) {
        try {
            this.value = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid user ID format");
        }
    }

    public ReconciliationId(UUID value) {
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
