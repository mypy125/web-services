package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserId;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.port.incoming.UserUseCase;
import com.mygitgor.user_service.domain.port.outgoing.KafkaEventPort;
import com.mygitgor.user_service.domain.port.outgoing.NotificationPort;
import com.mygitgor.user_service.domain.port.outgoing.UserRepositoryPort;
import com.mygitgor.user_service.domain.service.UserDomainService;
import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;
import com.mygitgor.user_service.infrastructure.shared.exception.UserNotFoundException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
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

    @Override
    public Mono<User> createUser(Email email, String fullName, UserRole role) {
        log.info("Creating user with email: {}", email);

        return userDomainService.validateEmailUniqueness(email)
                .then(Mono.fromCallable(() -> User.register(email, fullName, role)))
                .flatMap(userRepository::save)
                .doOnSuccess(user -> {
                    notificationPort.sendWelcomeEmail(email, fullName).subscribe();
                    kafkaEventPort.sendUserCreatedEvent(user).subscribe();
                });
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
                .doOnSuccess(user -> kafkaEventPort.sendUserUpdatedEvent(user).subscribe());
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
                    notificationPort.sendEmailVerifiedNotification(email).subscribe();
                    kafkaEventPort.sendEmailVerifiedEvent(user).subscribe();
                });
    }

    @Override
    public Mono<Void> deleteUser(Email email) {
        log.info("Deleting user: {}", email);

        return userRepository.deleteByEmail(email)
                .doOnSuccess(v -> kafkaEventPort.sendUserDeletedEvent(email).subscribe());
    }

    @Override
    public Mono<User> changePassword(Email email, String newPassword) {
        log.info("Changing password for user: {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {
                    return user;
                })
                .flatMap(userRepository::save)
                .doOnSuccess(user -> {
                    notificationPort.sendPasswordChangedNotification(email).subscribe();
                });
    }

    @Override
    public Mono<User> updateAccountStatus(Email email, AccountStatus status, String reason) {
        log.info("Updating account status for user: {} to {}", email, status);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {
                    switch (status) {
                        case BANNED -> user.ban();
                        case SUSPENDED -> user.suspend();
                        case ACTIVE -> user.activate();
                        default -> throw new DomainException("Unknown status: " + status);
                    }
                    return user;
                })
                .flatMap(userRepository::save)
                .doOnSuccess(user -> kafkaEventPort.sendUserStatusChangedEvent(user, reason).subscribe());
    }
}
