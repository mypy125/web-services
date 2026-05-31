package com.mygitgor.auth_service.infrastrucrure.persistance.repository;

import com.mygitgor.auth_service.infrastrucrure.persistance.entity.VerificationCodeEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface VerificationCodeR2dbcRepository extends ReactiveCrudRepository<VerificationCodeEntity, UUID> {
    Mono<VerificationCodeEntity> findByEmailAndOtpAndPurpose(String email, String otp, String purpose);
    Mono<VerificationCodeEntity> findByEmailAndOtpAndPurposeAndUsedFalse(String email, String otp, String purpose);
    Flux<VerificationCodeEntity> findByEmailAndPurpose(String email, String purpose);
    Flux<VerificationCodeEntity> findByEmail(String email);

    @Modifying
    @Query("UPDATE verification_codes SET status = 'INVALIDATED' WHERE email = :email AND purpose = :purpose")
    Mono<Integer> invalidateAllOtpsForEmailAndPurpose(String email, String purpose);

    @Modifying
    @Query("DELETE FROM verification_codes WHERE expires_at < :now")
    Mono<Integer> deleteExpiredCodes(LocalDateTime now);
    Mono<Long> countByEmailAndPurposeAndCreatedAtAfter(String email, String purpose, LocalDateTime since);
    Mono<Void> deleteByEmail(String email);
}
