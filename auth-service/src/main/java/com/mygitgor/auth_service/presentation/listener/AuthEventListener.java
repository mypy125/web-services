package com.mygitgor.auth_service.presentation.listener;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.model.event.OtpGeneratedEvent;
import com.mygitgor.auth_service.domain.user.event.UserLoggedInEvent;
import com.mygitgor.auth_service.domain.user.event.UserRegisteredEvent;
import com.mygitgor.auth_service.infrastrucrure.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventListener {
    private final NotificationPublisher notificationPublisher;
    private final UserServiceClient userServiceClient;
    private final CartServiceClient cartServiceClient;

    @EventListener
    public void handleOtpGenerated(OtpGeneratedEvent event) {
        log.debug("Handling OTP generated event for: {}", event.getEmail());

        notificationPublisher.sendOtpEmail(
                event.getEmail(),
                event.getOtp(),
                getSubject(event.getPurpose()),
                getText(event.getOtp(), event.getPurpose())
        );
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling user registration event for: {}", event.getEmail());

        if (event.getRole() == UserRole.ROLE_CUSTOMER) {
            cartServiceClient.createCart(event.getUserId())
                    .subscribe(
                            success -> log.debug("Cart created for user: {}", event.getUserId()),
                            error -> log.error("Failed to create cart for user: {}", event.getUserId(), error)
                    );
        }

        notificationPublisher.sendWelcomeEmail(event.getEmail(), event.getRole());
    }

    @EventListener
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        log.info("User logged in: {}", event.getEmail());

        userServiceClient.updateLastLogin(event.getEmail(), event.getOccurredAt())
                .subscribe();
    }

    private String getSubject(OtpPurpose purpose) {
        return switch (purpose) {
            case REGISTRATION -> "Verify Your Account";
            case LOGIN -> "Your Login OTP";
            case EMAIL_VERIFICATION -> "Verify Your Email";
            case PASSWORD_RESET -> "Reset Your Password";
        };
    }

    private String getText(String otp, OtpPurpose purpose) {
        return String.format("Your OTP for %s is: %s. Valid for 10 minutes.",
                purpose.name().toLowerCase(), otp);
    }
}
