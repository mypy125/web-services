package com.mygitgor.auth_service.infrastrucrure.persistance;

import com.mygitgor.auth_service.domain.auth.repository.BlacklistedTokenRepositoryPort;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.mapper.TokenMapper;
import com.mygitgor.auth_service.infrastrucrure.persistance.entity.BlacklistedTokenEntity;
import com.mygitgor.auth_service.infrastrucrure.persistance.repository.BlacklistedTokenR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BlacklistedTokenRepositoryAdapter implements BlacklistedTokenRepositoryPort {
    private final BlacklistedTokenR2dbcRepository r2dbcRepository;
    private final TokenMapper tokenMapper;

    @Override
    public Mono<Void> save(String token, UserId userId, LocalDateTime expiresAt) {
        BlacklistedTokenEntity blacklistedToken = new BlacklistedTokenEntity(
                token,
                UUID.fromString(userId.toString()),
                expiresAt
        );
        return r2dbcRepository.save(blacklistedToken)
                .doOnSuccess(saved -> log.debug("Token blacklisted for user: {}", userId))
                .doOnError(error -> log.error("Failed to blacklist token for user: {}", userId, error))
                .then();
    }

    @Override
    public Mono<Boolean> existsByToken(String token) {
        return r2dbcRepository.existsByToken(token)
                .doOnSuccess(exists -> log.debug("Token exists check: {} - {}", token, exists))
                .doOnError(error -> log.error("Failed to check token existence: {}", token, error));
    }

    @Override
    public Mono<Optional<String>> findTokenByValue(TokenValue tokenValue) {
        return r2dbcRepository.findByToken(tokenValue.toString())
                .map(entity -> Optional.of(entity.getToken()))
                .defaultIfEmpty(Optional.empty())
                .doOnSuccess(opt -> log.debug("Token lookup result for: {} - present: {}",
                        tokenValue, opt.isPresent()))
                .doOnError(error -> log.error("Failed to find token by value: {}", tokenValue, error));
    }

    @Override
    @Transactional
    public Mono<Integer> deleteExpiredTokens() {
        return r2dbcRepository.deleteByExpiresAtBefore(LocalDateTime.now())
                .doOnSuccess(deletedCount -> {
                    if (deletedCount > 0) {
                        log.info("Deleted {} expired blacklisted tokens", deletedCount);
                    }
                })
                .doOnError(error -> log.error("Failed to delete expired blacklisted tokens", error));
    }

    @Override
    public Mono<Void> deleteByUserId(UserId userId) {
        return r2dbcRepository.deleteByUserId(UUID.fromString(userId.toString()))
                .doOnSuccess(v -> log.debug("Deleted blacklisted tokens for user: {}", userId))
                .doOnError(error -> log.error("Failed to delete blacklisted tokens for user: {}", userId, error));
    }

    @Override
    public Mono<Long> countActiveBlacklistedTokens() {
        return r2dbcRepository.countByExpiresAtAfter(LocalDateTime.now())
                .doOnSuccess(count -> log.debug("Active blacklisted tokens count: {}", count))
                .doOnError(error -> log.error("Failed to count active blacklisted tokens", error));
    }
}
