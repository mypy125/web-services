package com.mygitgor.auth_service.domain.specification;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.enums.TokenStatus;
import com.mygitgor.auth_service.domain.auth.repository.BlacklistedTokenRepository;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenValiditySpecification {
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public boolean isSatisfiedBy(Token token) {
        return token != null
                && token.isValid()
                && !isBlacklisted(token);
    }

    public void check(Token token) {
        if (token == null) {
            throw new DomainException("Token is null");
        }
        if (token.isExpired()) {
            throw new DomainException("Token has expired");
        }
        if (token.getStatus() != TokenStatus.ACTIVE) {
            throw new DomainException("Token is not active");
        }
        if (isBlacklisted(token)) {
            throw new DomainException("Token is blacklisted");
        }
    }

    private boolean isBlacklisted(Token token) {
        return blacklistedTokenRepository.existsByToken(token.getValue().toString());
    }
}
