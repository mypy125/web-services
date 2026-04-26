package com.mygitgor.auth_service.domain.shared.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
public class Otp {
    private final String value;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    public Otp(String value, int validityMinutes) {
        if (value == null || value.length() != 6 || !value.matches("\\d{6}")) {
            throw new DomainException("OTP must be 6 digits");
        }
        this.value = value;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusMinutes(validityMinutes);
    }

    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiresAt);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
