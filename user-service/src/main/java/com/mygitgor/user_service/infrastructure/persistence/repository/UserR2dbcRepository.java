package com.mygitgor.user_service.infrastructure.persistence.repository;

import com.mygitgor.user_service.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserR2dbcRepository extends ReactiveCrudRepository<UserEntity, UUID> {
    Mono<UserEntity> findByEmail(String email);
    Flux<UserEntity> findAllBy(Pageable pageable);
    Flux<UserEntity> findByRole(String role);
    Flux<UserEntity> findByAccountStatus(String accountStatus);
    Flux<UserEntity> findByEmailContaining(String email);
    Flux<UserEntity> findByFullNameContaining(String fullName);
    Mono<Boolean> existsByEmail(String email);
    Mono<Void> deleteByEmail(String email);
    Mono<Void> deleteByAccountStatus(String accountStatus);
    Mono<Long> countByRole(String role);
    Mono<Long> countByAccountStatus(String accountStatus);
    Mono<Long> countByEmailVerifiedTrue();
    @Query("UPDATE users SET last_login_at = :lastLoginAt, updated_at = NOW() WHERE id = :userId")
    Mono<Void> updateLastLoginAt(UUID userId, java.time.LocalDateTime lastLoginAt);
    @Query("UPDATE users SET email_verified = true, email_verified_at = NOW(), updated_at = NOW() WHERE id = :userId")
    Mono<Void> updateEmailVerified(UUID userId);
    @Query("UPDATE users SET account_status = :status, updated_at = NOW() WHERE id = :userId")
    Mono<Void> updateAccountStatus(UUID userId, String status);
    @Query("UPDATE users SET total_orders_count = :totalOrders, total_spent_amount = :totalSpent, updated_at = NOW() WHERE id = :userId")
    Mono<Void> updateOrderStats(UUID userId, Integer totalOrders, Double totalSpent);
    @Query("SELECT * FROM users WHERE email ILIKE CONCAT('%', :searchTerm, '%') OR full_name ILIKE CONCAT('%', :searchTerm, '%')")
    Flux<UserEntity> searchByEmailOrFullName(String searchTerm);
    @Query("SELECT * FROM users WHERE email ILIKE CONCAT('%', :searchTerm, '%') OR full_name ILIKE CONCAT('%', :searchTerm, '%') LIMIT :limit OFFSET :offset")
    Flux<UserEntity> searchByEmailOrFullNameWithPagination(String searchTerm, int limit, int offset);
    @Query("SELECT * FROM users WHERE " +
            "(:email IS NULL OR email ILIKE CONCAT('%', :email, '%')) AND " +
            "(:fullName IS NULL OR full_name ILIKE CONCAT('%', :fullName, '%')) AND " +
            "(:role IS NULL OR role = :role) AND " +
            "(:accountStatus IS NULL OR account_status = :accountStatus)")
    Flux<UserEntity> searchByCriteria(String email, String fullName, String role, String accountStatus);
    Flux<UserEntity> findByIdIn(Iterable<UUID> ids);
    @Query("SELECT role, COUNT(*) as count FROM users GROUP BY role")
    Flux<RoleCountProjection> countUsersByRole();

    interface RoleCountProjection {
        String getRole();
        Long getCount();
    }

    @Query("SELECT account_status, COUNT(*) as count FROM users GROUP BY account_status")
    Flux<StatusCountProjection> countUsersByStatus();

    interface StatusCountProjection {
        String getAccountStatus();
        Long getCount();
    }

    @Query("SELECT * FROM users WHERE last_login_at IS NOT NULL ORDER BY last_login_at DESC LIMIT :limit")
    Flux<UserEntity> findMostRecentActiveUsers(int limit);

    @Query("SELECT * FROM users WHERE created_at >= :since")
    Flux<UserEntity> findUsersRegisteredSince(java.time.LocalDateTime since);
}
