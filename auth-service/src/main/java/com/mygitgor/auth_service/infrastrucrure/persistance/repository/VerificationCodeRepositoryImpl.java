package com.mygitgor.auth_service.infrastrucrure.persistance.repository;

import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.repository.VerificationCodeRepository;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VerificationCodeRepositoryImpl implements VerificationCodeRepository {
    private final VerificationCodeJpaRepository jpaRepository;

    @Override
    @Transactional
    public VerificationCode save(VerificationCode code) {
        VerificationCode entity = toEntity(code);
        VerificationCode saved = jpaRepository.save(entity);
        log.debug("Verification code saved for email: {}, purpose: {}", code.getEmail(), code.getPurpose());
        return toDomain(saved);
    }

    @Override
    public Optional<VerificationCode> findByEmailAndOtpAndPurpose(Email email, String otp, OtpPurpose purpose) {
        return jpaRepository.findByEmailAndOtpAndPurpose(email.toString(), otp, purpose.name())
                .map(this::toDomain);
    }

    @Override
    public Optional<VerificationCode> findValidOtp(Email email, String otp, OtpPurpose purpose, LocalDateTime now) {
        return jpaRepository.findByEmailAndOtpAndPurposeAndUsedFalse(email.toString(), otp, purpose.name())
                .filter(code -> code.getExpiresAt().isAfter(now))
                .map(this::toDomain);
    }

    @Override
    public List<VerificationCode> findByEmailAndPurpose(Email email, OtpPurpose purpose) {
        return jpaRepository.findByEmailAndPurpose(email.toString(), purpose.name())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<VerificationCode> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.toString())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void invalidateAllOtpsForEmailAndPurpose(Email email, OtpPurpose purpose) {
        jpaRepository.invalidateAllOtpsForEmailAndPurpose(email.toString(), purpose.name());
        log.debug("Invalidated all OTPs for email: {}, purpose: {}", email, purpose);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void deleteExpiredCodes() {
        jpaRepository.deleteExpiredCodes(LocalDateTime.now());
        log.debug("Deleted expired verification codes");
    }

    @Override
    public long countRecentOtps(Email email, OtpPurpose purpose, LocalDateTime since) {
        return jpaRepository.countByEmailAndPurposeAndCreatedAtAfter(
                email.toString(),
                purpose.name(),
                since
        );
    }

    @Override
    @Transactional
    public void delete(VerificationCode code) {
        jpaRepository.deleteById(UUID.fromString(code.getId()));
        log.debug("Deleted verification code for email: {}", code.getEmail());
    }

    @Override
    @Transactional
    public void deleteAllByEmail(Email email) {
        List<VerificationCode> codes = jpaRepository.findByEmail(email.toString())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());

        for (VerificationCode code : codes) {
            jpaRepository.deleteById(UUID.fromString(code.getId()));
        }
        log.debug("Deleted all verification codes for email: {}", email);
    }

    // Mappers
    private VerificationCode toDomain(com.mygitgor.auth_service.infrastructure.persistence.entity.VerificationCode entity) {
        if (entity == null) return null;

        return VerificationCode.builder()
                .id(entity.getId().toString())
                .otp(entity.getOtp())
                .email(new Email(entity.getEmail()))
                .role(entity.getUserRole())
                .purpose(OtpPurpose.valueOf(entity.getPurpose()))
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .used(entity.isUsed())
                .build();
    }

    private com.mygitgor.auth_service.infrastructure.persistence.entity.VerificationCode toEntity(VerificationCode domain) {
        if (domain == null) return null;

        com.mygitgor.auth_service.infrastructure.persistence.entity.VerificationCode entity =
                new com.mygitgor.auth_service.infrastructure.persistence.entity.VerificationCode();

        if (domain.getId() != null) {
            entity.setId(UUID.fromString(domain.getId()));
        }
        entity.setOtp(domain.getOtp().getValue());
        entity.setEmail(domain.getEmail().toString());
        entity.setUserRole(domain.getRole());
        entity.setPurpose(domain.getPurpose().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setUsed(domain.isUsed());

        return entity;
    }
}
