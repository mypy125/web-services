package com.mygitgor.auth_service.infrastrucrure.client;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.port.NotificationPort;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import reactor.core.publisher.Mono;

public class NotificationServiceClient implements NotificationPort {

    @Override
    public Mono<Void> sendOtpEmail(Email email, String otp, OtpPurpose purpose) {
        return null;
    }

    @Override
    public Mono<Void> sendWelcomeEmail(Email email, String name) {
        return null;
    }

    @Override
    public Mono<Void> sendEmailVerifiedNotification(Email email) {
        return null;
    }

    @Override
    public Mono<Void> sendPasswordChangedNotification(Email email) {
        return null;
    }
}
