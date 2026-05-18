package com.mygitgor.auth_service.infrastrucrure.persistance;

import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.repository.VerificationCodeRepository;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.infrastrucrure.mapper.VerificationCodeMapper;
import com.mygitgor.auth_service.infrastrucrure.persistance.entity.VerificationCodeEntity;
import com.mygitgor.auth_service.infrastrucrure.persistance.repository.VerificationCodeR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VerificationCodeRepositoryImpl implements VerificationCodeRepository {
    private final VerificationCodeR2dbcRepository r2dbcRepository;
    private final VerificationCodeMapper mapper;

    @Override
    public Mono<VerificationCode> save(VerificationCode code) {
        VerificationCodeEntity entity = mapper.toEntity(code);
        return r2dbcRepository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(saved -> log.debug("Verification code saved for email: {}, purpose: {}",
                        code.getEmail(), code.getPurpose()))
                .doOnError(error -> log.error("Failed to save verification code for email: {}",
                        code.getEmail(), error));
    }

    @Override
    public Mono<VerificationCode> findByEmailAndOtpAndPurpose(Email email, String otp, OtpPurpose purpose) {
        return r2dbcRepository.findByEmailAndOtpAndPurpose(email.toString(), otp, purpose.name())
                .map(mapper::toDomain)
                .doOnSuccess(code -> {
                    if (code != null) {
                        log.debug("Found verification code for email: {}, purpose: {}", email, purpose);
                    } else {
                        log.debug("No verification code found for email: {}, purpose: {}", email, purpose);
                    }
                })
                .doOnError(error -> log.error("Failed to find verification code for email: {}", email, error));
    }

    @Override
    public Mono<VerificationCode> findValidOtp(Email email, String otp, OtpPurpose purpose, LocalDateTime now) {
        return r2dbcRepository.findByEmailAndOtpAndPurposeAndUsedFalse(email.toString(), otp, purpose.name())
                .filter(entity -> entity.getExpiresAt().isAfter(now))
                .map(mapper::toDomain)
                .doOnSuccess(code -> {
                    if (code != null) {
                        log.debug("Found valid OTP for email: {}, purpose: {}", email, purpose);
                    } else {
                        log.debug("No valid OTP found for email: {}, purpose: {}", email, purpose);
                    }
                })
                .doOnError(error -> log.error("Failed to find valid OTP for email: {}", email, error));
    }

    @Override
    public Flux<VerificationCode> findByEmailAndPurpose(Email email, OtpPurpose purpose) {
        return r2dbcRepository.findByEmailAndPurpose(email.toString(), purpose.name())
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Found verification codes for email: {}, purpose: {}", email, purpose))
                .doOnError(error -> log.error("Failed to find verification codes for email: {}", email, error));
    }

    @Override
    public Flux<VerificationCode> findByEmail(Email email) {
        return r2dbcRepository.findByEmail(email.toString())
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Found verification codes for email: {}", email))
                .doOnError(error -> log.error("Failed to find verification codes for email: {}", email, error));
    }

    @Override
    public Mono<Void> invalidateAllOtpsForEmailAndPurpose(Email email, OtpPurpose purpose) {
        return r2dbcRepository.invalidateAllOtpsForEmailAndPurpose(email.toString(), purpose.name())
                .doOnSuccess(count -> log.debug("Invalidated {} OTPs for email: {}, purpose: {}",
                        count, email, purpose))
                .doOnError(error -> log.error("Failed to invalidate OTPs for email: {}", email, error))
                .then();
    }

    @Override
    public Mono<Integer> deleteExpiredCodes() {
        return r2dbcRepository.deleteExpiredCodes(LocalDateTime.now())
                .doOnSuccess(deletedCount -> {
                    if (deletedCount > 0) {
                        log.debug("Deleted {} expired verification codes", deletedCount);
                    }
                })
                .doOnError(error -> log.error("Failed to delete expired verification codes", error));
    }

    @Override
    public Mono<Long> countRecentOtps(Email email, OtpPurpose purpose, LocalDateTime since) {
        return r2dbcRepository.countByEmailAndPurposeAndCreatedAtAfter(
                        email.toString(),
                        purpose.name(),
                        since
                )
                .doOnSuccess(count -> log.debug("Recent OTPs count for email: {}, purpose: {} - count: {}",
                        email, purpose, count))
                .doOnError(error -> log.error("Failed to count recent OTPs for email: {}", email, error));
    }

    @Override
    public Mono<Void> delete(VerificationCode code) {
        return r2dbcRepository.deleteById(UUID.fromString(code.getId().toString()))
                .doOnSuccess(v -> log.debug("Deleted verification code for email: {}", code.getEmail()))
                .doOnError(error -> log.error("Failed to delete verification code for email: {}", code.getEmail(), error));
    }

    @Override
    public Mono<Void> deleteAllByEmail(Email email) {
        return r2dbcRepository.deleteByEmail(email.toString())
                .doOnSuccess(count -> log.debug("Deleted {} verification codes for email: {}", count, email))
                .doOnError(error -> log.error("Failed to delete verification codes for email: {}", email, error));
    }
}
