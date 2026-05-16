package com.mygitgor.auth_service.domain.auth.repository;

import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BlacklistedTokenRepository {
    Mono<Void> save(String token, UserId userId, LocalDateTime expiresAt);
    Mono<Boolean> existsByToken(String token);
    Mono<Optional<String>> findTokenByValue(TokenValue tokenValue);
    Mono<Integer> deleteExpiredTokens();
    Mono<Void> deleteByUserId(UserId userId);
    Mono<Long> countActiveBlacklistedTokens();
}
