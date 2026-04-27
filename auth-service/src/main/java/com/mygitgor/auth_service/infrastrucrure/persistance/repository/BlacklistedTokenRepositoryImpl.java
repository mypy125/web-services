package com.mygitgor.auth_service.infrastrucrure.persistance.repository;

import com.mygitgor.auth_service.domain.auth.repository.BlacklistedTokenRepository;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.persistance.entity.BlacklistedTokenEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BlacklistedTokenRepositoryImpl implements BlacklistedTokenRepository {
    private final BlacklistedTokenJpaRepository jpaRepository;

    @Override
    @Transactional
    public void save(String token, UserId userId, LocalDateTime expiresAt) {
        BlacklistedTokenEntity blacklistedToken = new BlacklistedTokenEntity(
                token,
                UUID.fromString(userId.toString()),
                expiresAt
        );
        jpaRepository.save(blacklistedToken);
        log.debug("Token blacklisted for user: {}", userId);
    }

    @Override
    public boolean existsByToken(String token) {
        return jpaRepository.existsByToken(token);
    }

    @Override
    public Optional<String> findTokenByValue(TokenValue tokenValue) {
        return jpaRepository.findByToken(tokenValue.toString())
                .map(BlacklistedTokenEntity::getToken);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * *") // Run daily at 2 AM
    public void deleteExpiredTokens() {
        int deletedCount = jpaRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("Deleted {} expired blacklisted tokens", deletedCount);
        }
    }

    @Override
    @Transactional
    public void deleteByUserId(UserId userId) {
        jpaRepository.deleteByUserId(UUID.fromString(userId.toString()));
        log.debug("Deleted blacklisted tokens for user: {}", userId);
    }

    @Override
    public long countActiveBlacklistedTokens() {
        return jpaRepository.countByExpiresAtAfter(LocalDateTime.now());
    }
}
