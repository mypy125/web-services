package com.mygitgor.auth_service.domain.auth.repository;

import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BlacklistedTokenRepository {
    void save(String token, UserId userId, LocalDateTime expiresAt);
    boolean existsByToken(String token);
    Optional<String> findTokenByValue(TokenValue tokenValue);
    void deleteExpiredTokens();
    void deleteByUserId(UserId userId);
    long countActiveBlacklistedTokens();
}
