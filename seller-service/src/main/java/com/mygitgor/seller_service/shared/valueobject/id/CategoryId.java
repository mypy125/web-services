package com.mygitgor.seller_service.shared.valueobject.id;

import com.mygitgor.seller_service.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;


@Getter
@EqualsAndHashCode
public class CategoryId {
    private final UUID value;

    public CategoryId() {
        this.value = UUID.randomUUID();
    }

    public CategoryId(String value) {
        try {
            this.value = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid user ID format");
        }
    }

    public CategoryId(UUID value) {
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