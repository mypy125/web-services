package com.mygitgor.auth_service.domain.auth.service;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.repository.TokenRepository;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.domain.specification.TokenValiditySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenDomainService {
    private final JwtPort jwtPort;
    private final TokenValiditySpecification tokenValiditySpec;

    public Token generateToken(Email email, UserId userId, UserRole role) {
        // 1. Generate JWT via infrastructure
        String jwtValue = jwtPort.generateToken(email.toString(), userId.toString(), role);

        // 2. Create token entity
        Token token = Token.builder()
                .value(new TokenValue(jwtValue))
                .email(email)
                .userId(userId)
                .role(role)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtPort.getTokenExpirationSeconds()))
                .status(TokenStatus.ACTIVE)
                .build();

        // 3. Validate token business rules
        tokenValiditySpec.check(token);

        // 4. Save
        return tokenRepository.save(token);
    }

    public boolean validateToken(TokenValue tokenValue) {
        Token token = tokenRepository.findByValue(tokenValue)
                .orElseThrow(() -> new DomainException("Token not found"));

        return token.isValid();
    }

    public Token getTokenInfo(TokenValue tokenValue) {
        return tokenRepository.findByValue(tokenValue)
                .orElseThrow(() -> new DomainException("Token not found"));
    }

    public void blacklistToken(Token token) {
        if (!token.isValid()) {
            throw new DomainException("Cannot blacklist invalid or expired token");
        }

        token.blacklist();
        tokenRepository.save(token);

        log.info("Token blacklisted for user: {}", token.getEmail());
    }
}
