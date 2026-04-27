package com.mygitgor.auth_service.infrastrucrure.persistance.repository;

import com.mygitgor.auth_service.infrastrucrure.persistance.entity.BlacklistedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface BlacklistedTokenJpaRepository extends JpaRepository<BlacklistedTokenEntity, UUID> {
    boolean existsByToken(String token);
    Optional<BlacklistedTokenEntity> findByToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
