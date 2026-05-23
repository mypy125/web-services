package com.mygitgor.auth_service.application.service;

import com.mygitgor.auth_service.application.command.RequestOtpCommand;
import com.mygitgor.auth_service.application.command.VerifyOtpCommand;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.service.OtpDomainService;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpApplicationService {
    private final OtpDomainService otpDomainService;

    @Transactional
    public Mono<Void> requestOtp(RequestOtpCommand command) {
        return Mono.fromCallable(() -> {
                    Email email = new Email(command.getEmail());
                    UserRole role = command.getRole();
                    OtpPurpose purpose = command.getPurpose();
                    return otpDomainService.generateOtp(email, role, purpose);
                })
                .doOnNext(verificationCode ->
                        log.info("OTP requested for email: {}, purpose: {}", command.getEmail(), command.getPurpose()))
                .doOnError(error -> log.error("Failed to request OTP for: {}, error: {}",
                        command.getEmail(), error.getMessage()))
                .onErrorResume(error -> Mono.error(new DomainException("Failed to generate OTP: " + error.getMessage())))
                .then();
    }

    public Mono<Boolean> verifyOtp(VerifyOtpCommand command) {
        return Mono.fromCallable(() -> new Email(command.getEmail()))
                .flatMap(emailVo -> {
                    OtpPurpose purpose = OtpPurpose.valueOf(command.getPurpose());
                    return otpDomainService.validateOtp(emailVo, command.getOtp(), purpose);
                })
                .doOnSuccess(isValid -> {
                    if (Boolean.TRUE.equals(isValid)) {
                        log.info("OTP verified successfully for email: {}", command.getEmail());
                    } else {
                        log.warn("Invalid OTP for email: {}", command.getEmail());
                    }
                })
                .doOnError(error -> log.error("Failed to verify OTP for: {}, error: {}",
                        command.getEmail(), error.getMessage()));
    }

}
