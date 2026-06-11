package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.port.incoming.UserUseCase;
import com.mygitgor.user_service.domain.port.outgoing.KafkaEventPort;
import com.mygitgor.user_service.domain.port.outgoing.NotificationPort;
import com.mygitgor.user_service.domain.port.outgoing.UserRepositoryPort;
import com.mygitgor.user_service.domain.service.UserDomainService;
import com.mygitgor.user_service.infrastructure.mapper.UserDtoMapper;
import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;
import com.mygitgor.user_service.infrastructure.shared.exception.UserNotFoundException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService implements UserUseCase {
    private final UserRepositoryPort userRepository;
    private final UserDomainService userDomainService;
    private final NotificationPort notificationPort;
    private final KafkaEventPort kafkaEventPort;
    private final UserDtoMapper userMapper;

    @Override
    public Mono<User> createUser(Email email, String fullName, String phoneNumber, UserRole role) {
        log.info("Creating user with email: {}", email);

        return userDomainService.validateEmailUniqueness(email)
                .then(Mono.fromCallable(() -> User.register(email, fullName, phoneNumber, role)))
                .flatMap(userRepository::save)
                .doOnSuccess(user -> {
                    log.info("User created successfully: {}", email);
                    notificationPort.sendWelcomeEmail(email, fullName).subscribe(
                            success -> log.debug("Welcome email sent to: {}", email),
                            error -> log.error("Failed to send welcome email to: {}", email, error)
                    );
                    kafkaEventPort.sendUserCreatedEvent(user).subscribe(
                            success -> log.debug("User created event sent to Kafka"),
                            error -> log.error("Failed to send user created event to Kafka", error)
                    );
                })
                .doOnError(error -> log.error("Failed to create user: {}", email, error));
    }

    @Override
    public Mono<User> updateUser(UserId userId, String fullName, String phoneNumber, String profileImage) {
        log.info("Updating user: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    user.updateProfile(fullName, profileImage, phoneNumber);
                    return user;
                })
                .flatMap(userRepository::save)
                .doOnSuccess(user -> {
                    log.info("User updated successfully: {}", userId);
                    kafkaEventPort.sendUserUpdatedEvent(user).subscribe(
                            success -> log.debug("User updated event sent to Kafka"),
                            error -> log.error("Failed to send user updated event to Kafka", error)
                    );
                });
    }

    @Override
    public Mono<User> verifyEmail(Email email) {
        log.info("Verifying email for user: {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {
                    user.verifyEmail();
                    return user;
                })
                .flatMap(userRepository::save)
                .doOnSuccess(user -> {
                    log.info("Email verified successfully for: {}", email);
                    notificationPort.sendEmailVerifiedNotification(email).subscribe(
                            success -> log.debug("Email verified notification sent to: {}", email),
                            error -> log.error("Failed to send email verified notification to: {}", email, error)
                    );
                    kafkaEventPort.sendEmailVerifiedEvent(user).subscribe(
                            success -> log.debug("Email verified event sent to Kafka"),
                            error -> log.error("Failed to send email verified event to Kafka", error)
                    );
                });
    }

    @Override
    public Mono<Void> deleteUser(Email email) {
        log.info("Deleting user: {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .flatMap(user -> userRepository.deleteByEmail(email))
                .doOnSuccess(v -> {
                    log.info("User deleted successfully: {}", email);
                    kafkaEventPort.sendUserDeletedEvent(email).subscribe(
                            success -> log.debug("User deleted event sent to Kafka"),
                            error -> log.error("Failed to send user deleted event to Kafka", error)
                    );
                })
                .doOnError(error -> log.error("Failed to delete user: {}", email, error));
    }

    @Override
    public Mono<User> changePassword(Email email, String newPassword) {
        log.info("Changing password for user: {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {

                    user.updateLastLogin();
                    return user;
                })
                .flatMap(userRepository::save)
                .doOnSuccess(user -> {
                    log.info("Password changed for user: {}", email);
                    notificationPort.sendPasswordChangedNotification(email).subscribe(
                            success -> log.debug("Password changed notification sent to: {}", email),
                            error -> log.error("Failed to send password changed notification to: {}", email, error)
                    );
                    kafkaEventPort.sendPasswordChangedEvent(email).subscribe(
                            success -> log.debug("Password changed event sent to Kafka"),
                            error -> log.error("Failed to send password changed event to Kafka", error)
                    );
                });
    }

    @Override
    public Mono<User> updateAccountStatus(Email email, AccountStatus status, String reason) {
        log.info("Updating account status for user: {} to {}", email, status);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {
                    AccountStatus oldStatus = user.getAccountStatus();
                    switch (status) {
                        case BANNED -> user.ban();
                        case SUSPENDED -> user.suspend();
                        case ACTIVE -> user.activate();
                        default -> throw new DomainException("Unknown status: " + status);
                    }
                    return new Object[]{user, oldStatus};
                })
                .flatMap(arr -> userRepository.save((User) arr[0])
                        .map(user -> new Object[]{user, arr[1]}))
                .doOnSuccess(arr -> {
                    User user = (User) arr[0];
                    AccountStatus oldStatus = (AccountStatus) arr[1];
                    log.info("Account status updated for user: {} from {} to {}", email, oldStatus, status);
                    kafkaEventPort.sendUserStatusChangedEvent(user, oldStatus.name(), reason, changedBy).subscribe(
                            success -> log.debug("User status changed event sent to Kafka"),
                            error -> log.error("Failed to send user status changed event to Kafka", error)
                    );
                })
                .map(arr -> (User) arr[0]);
    }
}
