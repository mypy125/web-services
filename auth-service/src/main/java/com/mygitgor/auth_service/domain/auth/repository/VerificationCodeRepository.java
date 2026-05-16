package com.mygitgor.auth_service.domain.auth.repository;

import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository {
    Mono<VerificationCode> save(VerificationCode code);
    Mono<Optional<VerificationCode>> findByEmailAndOtpAndPurpose(Email email, String otp, OtpPurpose purpose);
    Mono<Optional<VerificationCode>> findValidOtp(Email email, String otp, OtpPurpose purpose, LocalDateTime now);
    Flux<VerificationCode> findByEmailAndPurpose(Email email, OtpPurpose purpose);
    Flux<VerificationCode> findByEmail(Email email);
    Mono<Void> invalidateAllOtpsForEmailAndPurpose(Email email, OtpPurpose purpose);
    Mono<Integer> deleteExpiredCodes();
    Mono<Long> countRecentOtps(Email email, OtpPurpose purpose, LocalDateTime since);
    Mono<Void> delete(VerificationCode code);
    Mono<Void> deleteAllByEmail(Email email);
}
