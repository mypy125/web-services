package com.mygitgor.auth_service.domain.model;

import com.mygitgor.auth_service.domain.model.enums.TokenStatus;
import com.mygitgor.auth_service.domain.model.enums.UserRole;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Token {
    private final TokenValue value;
    private final Email email;
    private final UserId userId;
    private final UserRole role;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private TokenStatus status;

    public void blacklist() {
        if (this.status == TokenStatus.BLACKLISTED) {
            throw new DomainException("Token is already blacklisted");
        }
        if (isExpired()) {
            throw new DomainException("Cannot blacklist expired token");
        }
        this.status = TokenStatus.BLACKLISTED;
    }

    public boolean isValid() {
        return status == TokenStatus.ACTIVE && !isExpired();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void validate() {
        if (!isValid()) {
            throw new DomainException("Token is invalid or expired");
        }
    }
}
