package com.mygitgor.auth_service.domain.auth.service;

import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.port.NotificationPort;
import com.mygitgor.auth_service.domain.auth.repository.VerificationCodeRepositoryPort;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.Otp;
import com.mygitgor.auth_service.domain.specification.OtpValiditySpecification;
import com.mygitgor.auth_service.infrastrucrure.config.KafkaConfig;
import com.mygitgor.auth_service.infrastrucrure.kafka.event.OtpGeneratedEvent;
import com.mygitgor.auth_service.infrastrucrure.kafka.event.OtpVerifiedEvent;
import com.mygitgor.auth_service.infrastrucrure.kafka.producer.KafkaEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpDomainService {
    private static final int OTP_VALIDITY_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final VerificationCodeRepositoryPort verificationCodeRepository;
    private final OtpValiditySpecification otpValiditySpec;
    private final NotificationPort notificationPort;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional
    public Mono<VerificationCode> generateOtp(Email email, UserRole role, OtpPurpose purpose) {
        log.info("Generating OTP for email: {}, purpose: {}", email, purpose);

        return invalidateExistingOtps(email, purpose)
                .then(Mono.fromCallable(() -> {
                    String otpValue = generateSecureOtp();
                    Otp otp = new Otp(otpValue, OTP_VALIDITY_MINUTES);

                    return VerificationCode.builder()
                            .id(UUID.randomUUID())
                            .otp(otp)
                            .email(email)
                            .userRole(role)
                            .purpose(purpose)
                            .createdAt(LocalDateTime.now())
                            .used(false)
                            .build();
                }))
                .flatMap(verificationCodeRepository::save)
                .doOnNext(savedCode -> {
                    sendKafkaOtpGeneratedEvent(email, savedCode, purpose).subscribe();

                    notificationPort.sendOtpEmail(email, savedCode.getOtp().getValue(), purpose)
                            .subscribe(
                                    success -> log.info("OTP email sent successfully to: {}", email),
                                    error -> log.error("Failed to send OTP email to: {}, error: {}", email, error.getMessage())
                            );
                })
                .doOnSuccess(savedCode -> log.info("OTP generated for email: {}, purpose: {}", email, purpose))
                .doOnError(error -> log.error("Failed to generate OTP for email: {}", email, error));
    }

    @Transactional
    public Mono<Void> generateAndSendOtp(Email email, UserRole role, OtpPurpose purpose) {
        log.info("Generating and sending OTP for email: {}, purpose: {}", email, purpose);

        return invalidateExistingOtps(email, purpose)
                .then(Mono.fromCallable(() -> {
                    String otpValue = generateSecureOtp();
                    Otp otp = new Otp(otpValue, OTP_VALIDITY_MINUTES);

                    return VerificationCode.builder()
                            .id(UUID.randomUUID())
                            .otp(otp)
                            .email(email)
                            .userRole(role)
                            .purpose(purpose)
                            .createdAt(LocalDateTime.now())
                            .used(false)
                            .build();
                }))
                .flatMap(verificationCodeRepository::save)
                .flatMap(savedCode -> {
                    return sendKafkaOtpGeneratedEvent(email, savedCode, purpose)
                            .then(notificationPort.sendOtpEmail(email, savedCode.getOtp().getValue(), purpose))
                            .thenReturn(savedCode);
                })
                .doOnSuccess(savedCode -> log.info("OTP generated and sent successfully to: {}", email))
                .doOnError(error -> log.error("Failed to generate/send OTP for {}: {}", email, error.getMessage()))
                .then();
    }

    public Mono<Boolean> validateOtp(Email email, String otpValue, OtpPurpose purpose) {
        log.debug("Validating OTP for email: {}, purpose: {}", email, purpose);

        return verificationCodeRepository.findByEmailAndOtpAndPurpose(email, otpValue, purpose)
                .flatMap(verificationCode -> {
                    try {
                        otpValiditySpec.check(verificationCode);
                        verificationCode.markAsUsed();
                        return verificationCodeRepository.save(verificationCode)
                                .flatMap(saved -> {
                                    sendKafkaOtpVerifiedEvent(email, purpose, true).subscribe();
                                    return Mono.just(true);
                                });
                    } catch (DomainException e) {
                        sendKafkaOtpVerifiedEvent(email, purpose, false).subscribe();
                        return Mono.error(e);
                    }
                })
                .doOnSuccess(valid -> {
                    if (Boolean.TRUE.equals(valid)) {
                        log.info("OTP validated for email: {}, purpose: {}", email, purpose);
                    }
                })
                .onErrorResume(e -> {
                    log.warn("OTP validation failed for email: {}, error: {}", email, e.getMessage());
                    return Mono.just(false);
                });
    }

    private String generateSecureOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private Mono<Void> invalidateExistingOtps(Email email, OtpPurpose purpose) {
        return verificationCodeRepository.findByEmailAndPurpose(email, purpose)
                .flatMap(code -> {
                    if (code.isValid()) {
                        code.markAsUsed();
                        return verificationCodeRepository.save(code);
                    }
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Void> sendKafkaOtpGeneratedEvent(Email email, VerificationCode code, OtpPurpose purpose) {
        OtpGeneratedEvent event = OtpGeneratedEvent.builder()
                .email(email.toString())
                .otp(code.getOtp().getValue())
                .purpose(purpose.name())
                .expiresAt(code.getOtp().getExpiresAt())
                .occurredAt(LocalDateTime.now())
                .build();

        return kafkaEventProducer.sendEvent(KafkaConfig.OTP_GENERATED_TOPIC, event);
    }

    private Mono<Void> sendKafkaOtpVerifiedEvent(Email email, OtpPurpose purpose, boolean success) {
        OtpVerifiedEvent event = OtpVerifiedEvent.builder()
                .email(email.toString())
                .purpose(purpose.name())
                .success(success)
                .occurredAt(LocalDateTime.now())
                .build();

        String topic = success ? KafkaConfig.OTP_VERIFIED_SUCCESS_TOPIC : KafkaConfig.OTP_VERIFIED_FAILURE_TOPIC;
        return kafkaEventProducer.sendEvent(topic, event);
    }
}

