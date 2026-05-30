package com.mygitgor.auth_service.domain.auth.model.port;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import reactor.core.publisher.Mono;

public interface NotificationPublisher {
    Mono<Void> sendOtpEmail(Email email, String otp, OtpPurpose purpose);
    Mono<Void> sendWelcomeEmail(Email email, String name);
    Mono<Void> sendEmailVerifiedNotification(Email email);
    Mono<Void> sendPasswordChangedNotification(Email email);
}
