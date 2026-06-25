package com.mygitgor.seller_service.domain.model.shared.valueobject.id;

import com.mygitgor.seller_service.domain.model.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class StatisticsId {
    private final UUID value;

    public StatisticsId() {
        this.value = UUID.randomUUID();
    }

    public StatisticsId(String value) {
        try {
            this.value = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid user ID format");
        }
    }

    public StatisticsId(UUID value) {
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