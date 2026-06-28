package com.mygitgor.seller_service.shared.valueobject.id;

import com.mygitgor.seller_service.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;


@Getter
@EqualsAndHashCode
public class ProductId {
    private final UUID value;

    public ProductId() {
        this.value = UUID.randomUUID();
    }

    public ProductId(String value) {
        try {
            this.value = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid user ID format");
        }
    }

    public ProductId(UUID value) {
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
