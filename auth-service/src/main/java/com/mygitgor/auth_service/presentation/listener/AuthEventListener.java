package com.mygitgor.auth_service.presentation.listener;

import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.port.NotificationPort;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.client.CartServiceClient;
import com.mygitgor.auth_service.infrastrucrure.client.UserServiceClient;
import com.mygitgor.auth_service.infrastrucrure.kafka.event.OtpGeneratedEvent;
import com.mygitgor.auth_service.infrastrucrure.kafka.event.UserLoggedInEvent;
import com.mygitgor.auth_service.infrastrucrure.kafka.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventListener {
    private final NotificationPort notificationPort;
    private final UserServiceClient userServiceClient;
    private final CartServiceClient cartServiceClient;

    @KafkaListener(topics = "otp.generated", groupId = "auth-service-group")
    public void handleOtpGenerated(OtpGeneratedEvent event) {
        log.debug("Handling OTP generated event for: {}", event.getEmail());

        notificationPort.sendOtpEmail(
                new Email(event.getEmail()),
                event.getOtp(),
                OtpPurpose.valueOf(event.getPurpose())
        ).subscribe(
                success -> log.debug("OTP email sent to: {}", event.getEmail()),
                error -> log.error("Failed to send OTP email to: {}", event.getEmail(), error)
        );
    }

    @KafkaListener(topics = "user.registered", groupId = "auth-service-group")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling user registration event for: {}", event.getEmail());

        if (UserRole.ROLE_CUSTOMER.name().equals(event.getRole())) {
            cartServiceClient.createCart(new UserId(event.getUserId()))
                    .subscribe(
                            success -> log.debug("Cart created for user: {}", event.getUserId()),
                            error -> log.error("Failed to create cart for user: {}", event.getUserId(), error)
                    );
        }

        notificationPort.sendWelcomeEmail(
                new Email(event.getEmail()),
                event.getEmail()
        ).subscribe(
                success -> log.debug("Welcome email sent to: {}", event.getEmail()),
                error -> log.error("Failed to send welcome email to: {}", event.getEmail(), error)
        );
    }

    @KafkaListener(topics = "user.logged.in", groupId = "auth-service-group")
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        log.info("User logged in: {}", event.getEmail());

        userServiceClient.updateLastLogin(new Email(event.getEmail()), event.getOccurredAt())
                .subscribe(
                        success -> log.debug("Last login updated for: {}", event.getEmail()),
                        error -> log.error("Failed to update last login for: {}", event.getEmail(), error)
                );
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
