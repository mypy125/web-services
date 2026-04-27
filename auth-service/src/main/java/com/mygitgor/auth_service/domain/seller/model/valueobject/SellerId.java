package com.mygitgor.auth_service.domain.seller.model.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class SellerId {
    private final UUID value;

    public SellerId() {
        this.value = UUID.randomUUID();
    }

    public SellerId(String value) {
        try {
            this.value = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid seller ID format");
        }
    }

    public SellerId(UUID value) {
        if (value == null) {
            throw new DomainException("Seller ID cannot be null");
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
