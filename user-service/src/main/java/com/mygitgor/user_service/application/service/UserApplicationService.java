package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.domain.port.incoming.UserUseCase;
import com.mygitgor.user_service.domain.port.outgoing.FileStoragePort;
import com.mygitgor.user_service.domain.port.outgoing.KafkaEventPort;
import com.mygitgor.user_service.domain.port.outgoing.NotificationPort;
import com.mygitgor.user_service.domain.repository.UserRepositoryPort;
import com.mygitgor.user_service.domain.service.UserDomainService;
import com.mygitgor.user_service.domain.service.UserValidationService;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateProfileRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateUserRequest;
import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;
import com.mygitgor.user_service.infrastructure.shared.exception.UserNotFoundException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserAuthInfo;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService implements UserUseCase {
    private final UserRepositoryPort userRepository;
    private final UserDomainService userDomainService;
    private final UserValidationService validationService;
    private final NotificationPort notificationPort;
    private final KafkaEventPort kafkaEventPort;
    private final FileStoragePort fileStoragePort;

    @Override
    @Transactional
    public Mono<User> createUser(Email email, String fullName, String phoneNumber, UserRole role) {
        log.info("Creating user with email: {}", email);
        return Mono.fromRunnable(() -> {
                    validationService.validateEmail(email);
                    validationService.validateFullName(fullName);
                    if (phoneNumber != null) validationService.validatePhoneNumber(phoneNumber);
                    validationService.validateRole(role);
                })
                .then(userDomainService.validateEmailUniqueness(email))
                .then(Mono.fromCallable(() -> User.register(email, fullName, phoneNumber, role)))
                .flatMap(userRepository::save)
                .delayUntil(user -> notificationPort.sendWelcomeEmail(email, fullName)
                        .doOnSuccess(v -> log.debug("Welcome email sent to: {}", email))
                        .doOnError(e -> log.error("Failed to send welcome email to: {}", email, e))
                        .onErrorResume(e -> Mono.empty()))
                .delayUntil(user -> kafkaEventPort.sendUserCreatedEvent(user)
                        .doOnSuccess(v -> log.debug("User created event sent to Kafka")));

    }

    @Override
    @Transactional
    public Mono<User> updateUser(UserId userId, UpdateUserRequest req) {
        log.info("Updating user: {}", userId);

        return Mono.fromRunnable(() -> {
                    if (req.fullName() != null) validationService.validateFullName(req.fullName());
                    if (req.phoneNumber() != null) validationService.validatePhoneNumber(req.phoneNumber());
                })
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    user.updateProfile(req.fullName(), req.profileImage(), req.phoneNumber());
                    return user;
                })
                .flatMap(userRepository::save)
                .delayUntil(kafkaEventPort::sendUserUpdatedEvent);
    }

    @Override
    @Transactional
    public Mono<User> verifyEmail(Email email) {
        log.info("Verifying email for user: {}", email);

        return Mono.fromRunnable(() -> validationService.validateEmail(email))
                .then(userRepository.findByEmail(email))
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {
                    user.verifyEmail();
                    return user;
                })
                .flatMap(userRepository::save)
                .delayUntil(user -> notificationPort.sendEmailVerifiedNotification(email)
                        .onErrorResume(e -> Mono.empty()))
                .delayUntil(kafkaEventPort::sendEmailVerifiedEvent);
    }

    @Override
    @Transactional
    public Mono<Void> deleteUser(Email email) {
        log.info("Deleting user: {}", email);

        return Mono.fromRunnable(() -> validationService.validateEmail(email))
                .then(userRepository.findByEmail(email))
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .delayUntil(userDomainService::validateAccountStatusForLogin)
                .flatMap(user -> userRepository.deleteByEmail(email)
                        .then(kafkaEventPort.sendUserDeletedEvent(email)));
    }

    @Override
    @Transactional
    public Mono<User> changePassword(Email email, String newPassword) {
        log.info("Changing password for user: {}", email);

        return Mono.fromRunnable(() -> {
                    validationService.validateEmail(email);
                    validationService.validatePassword(newPassword);
                })
                .then(userRepository.findByEmail(email))
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {
                    user.updateLastLogin();
                    return user;
                })
                .flatMap(userRepository::save)
                .delayUntil(user -> notificationPort.sendPasswordChangedNotification(email)
                        .onErrorResume(e -> Mono.empty()))
                .delayUntil(user -> kafkaEventPort.sendPasswordChangedEvent(email));
    }

    @Override
    @Transactional
    public Mono<User> updateAccountStatus(Email email, AccountStatus status, String reason, String changedBy) {
        log.info("Updating account status for user: {} to {}", email, status);

        return Mono.fromRunnable(() -> {
                    validationService.validateEmail(email);
                    validationService.validateAccountStatus(status);
                })
                .then(userRepository.findByEmail(email))
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .handle((user, sink) -> {
                    AccountStatus oldStatus = user.getAccountStatus();
                    try {
                        switch (status) {
                            case BANNED -> user.ban();
                            case SUSPENDED -> user.suspend();
                            case ACTIVE -> user.activate();
                            default -> throw new DomainException("Unsupported status transition: " + status);
                        }
                        sink.next(new Object[]{user, oldStatus});
                    } catch (DomainException e) {
                        sink.error(e);
                    }
                })
                .cast(Object[].class)
                .flatMap(arr -> userRepository.save((User) arr[0])
                        .thenReturn(arr))
                .flatMap(arr -> kafkaEventPort.sendUserStatusChangedEvent(
                                (User) arr[0],
                                ((AccountStatus) arr[1]).name(),
                                reason,
                                changedBy)
                        .thenReturn((User) arr[0]));
    }

    @Override
    public Mono<User> getUserById(UserId userId) {
        log.debug("Getting user by ID: {}", userId);
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)));
    }

    @Override
    @Transactional
    public Mono<User> updateProfile(UserId userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        return Mono.fromRunnable(() -> {
                    validationService.validateUserId(userId.toString());
                    validationService.validateProfileUpdate(
                            request.fullName(),
                            request.phoneNumber(),
                            request.profileImage()
                    );
                })
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .flatMap(user -> userDomainService.updateUserProfile(
                        user,
                        request.fullName(),
                        request.phoneNumber(),
                        request.profileImage()
                ))
                .flatMap(userRepository::save)
                .doOnSuccess(user -> {
                    log.info("Profile updated successfully for user: {}", userId);
                    kafkaEventPort.sendUserUpdatedEvent(user).subscribe();
                });
    }

    @Override
    @Transactional
    public Mono<User> uploadProfileImage(UserId userId, Mono<FilePart> filePart) {
        log.info("Uploading profile image for user: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .flatMap(user -> filePart
                        .filter(file -> !file.filename().isBlank())
                        .switchIfEmpty(Mono.error(new DomainException("File name is required")))
                        .flatMap(file -> fileStoragePort.uploadFile(file, "profile-images"))
                        .map(imageUrl -> {
                            user.updateProfile(null, imageUrl, null);
                            return user;
                        })
                )
                .flatMap(userRepository::save)
                .delayUntil(kafkaEventPort::sendUserUpdatedEvent);
    }

    @Override
    @Transactional
    public Mono<User> deleteProfileImage(UserId userId) {
        log.info("Deleting profile image for user: {}", userId);

        return Mono.fromRunnable(() -> validationService.validateUserId(userId.toString()))
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .flatMap(user -> userDomainService.updateUserProfile(user, null, null, null))
                .flatMap(userRepository::save)
                .doOnSuccess(user -> {
                    log.info("Profile image deleted for user: {}", userId);
                    kafkaEventPort.sendUserUpdatedEvent(user).subscribe();
                });
    }

    @Override
    @Transactional
    public Mono<Void> deleteUserById(UserId userId) {
        log.info("Deleting user by ID: {}", userId);

        return Mono.fromRunnable(() -> validationService.validateUserId(userId.toString()))
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .flatMap(user -> userDomainService.validateAccountStatusForLogin(user)
                        .then(userRepository.deleteByEmail(user.getEmail())))
                .doOnSuccess(v -> {
                    log.info("User deleted successfully: {}", userId);
                    kafkaEventPort.sendUserDeletedEvent(null).subscribe();
                });
    }

    @Override
    @Transactional
    public Mono<User> verifyEmailById(UserId userId) {
        log.info("Verifying email for user ID: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    user.verifyEmail();
                    return user;
                })
                .flatMap(userRepository::save)
                .delayUntil(user -> notificationPort.sendEmailVerifiedNotification(user.getEmail())
                        .onErrorResume(e -> Mono.empty()))
                .delayUntil(kafkaEventPort::sendEmailVerifiedEvent);
    }

    @Override
    @Transactional
    public Mono<User> changePasswordById(UserId userId, String newPassword) {
        log.info("Changing password for user ID: {}", userId);

        return Mono.fromRunnable(() -> {
                    validationService.validateUserId(userId.toString());
                    validationService.validatePassword(newPassword);
                })
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    user.updateLastLogin();
                    return user;
                })
                .flatMap(userRepository::save)
                .delayUntil(user -> notificationPort.sendPasswordChangedNotification(user.getEmail())
                        .onErrorResume(e -> Mono.empty()))
                .delayUntil(user -> kafkaEventPort.sendPasswordChangedEvent(user.getEmail()));
    }

    @Override
    public Mono<User> getUserByEmail(Email email) {
        log.debug("Getting user by email: {}", email);
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)));
    }

    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        log.debug("Checking if user exists by email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public Mono<Boolean> isEmailVerified(Email email) {
        log.debug("Checking if email is verified: {}", email);
        return userRepository.findByEmail(email)
                .map(User::isEmailVerified)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Page<User>> searchUsers(String searchTerm, int page, int size) {
        log.debug("Searching users with term: {}, page: {}, size: {}", searchTerm, page, size);
        return userRepository.search(searchTerm, page, size);
    }

    @Override
    public Flux<User> getUsersByIds(List<UserId> userIds) {
        log.debug("Getting users by IDs: {}", userIds);
        return userRepository.findByIds(userIds);
    }

    @Override
    public Mono<UserStatistics> getUserStatistics(UserId userId) {
        log.debug("Getting statistics for user: {}", userId);
        return userRepository.getStatistics(userId)
                .switchIfEmpty(Mono.fromCallable(() -> UserStatistics.create(userId)));
    }

    @Override
    public Mono<Long> countUsersByStatus(AccountStatus status) {
        log.debug("Counting users by status: {}", status);
        return userRepository.countByStatus(status);
    }

    @Override
    public Mono<Long> countUsersByRole(UserRole role) {
        log.debug("Counting users by role: {}", role);
        return userRepository.countByRole(role);
    }

    @Override
    @Transactional
    public Mono<User> activateUser(UserId userId, String activatedBy) {
        log.info("Activating user: {} by {}", userId, activatedBy);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    user.activate();
                    return user;
                })
                .flatMap(userRepository::save)
                .delayUntil(user -> kafkaEventPort.sendUserStatusChangedEvent(user, "INACTIVE", "Activated by admin", activatedBy))
                .delayUntil(user -> notificationPort.sendAccountActivatedNotification(user.getEmail()).onErrorResume(e -> Mono.empty()));
    }

    @Override
    @Transactional
    public Mono<User> banUser(UserId userId, String reason, String bannedBy) {
        log.info("Banning user: {} by {}, reason: {}", userId, bannedBy, reason);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    user.ban();
                    return user;
                })
                .flatMap(userRepository::save)
                .delayUntil(user -> kafkaEventPort.sendUserStatusChangedEvent(user, "ACTIVE", reason, bannedBy))
                .delayUntil(user -> notificationPort.sendAccountBannedNotification(user.getEmail(), reason).onErrorResume(e -> Mono.empty()));
    }

    @Override
    @Transactional
    public Mono<User> suspendUser(UserId userId, String reason, String suspendedBy) {
        log.info("Suspending user: {} by {}, reason: {}", userId, suspendedBy, reason);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    user.suspend();
                    return user;
                })
                .flatMap(userRepository::save)
                .delayUntil(user -> kafkaEventPort.sendUserStatusChangedEvent(user, "ACTIVE", reason, suspendedBy))
                .delayUntil(user -> notificationPort.sendAccountSuspendedNotification(user.getEmail(), reason).onErrorResume(e -> Mono.empty()));
    }

    @Override
    @Transactional
    public Mono<User> updateUserRole(UserId userId, UserRole newRole, String changedBy) {
        log.info("Updating user role: {} to {} by {}", userId, newRole, changedBy);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .flatMap(user -> {
                    UserRole oldRole = user.getRole();
                    user.updateRole(newRole);
                    return userRepository.save(user)
                            .delayUntil(savedUser -> kafkaEventPort.sendUserRoleChangedEvent(savedUser, oldRole.name(), newRole.name(), changedBy));
                });
    }

    @Override
    @Transactional
    public Mono<Void> updateLastLogin(UserId userId, LocalDateTime lastLoginAt) {
        log.debug("Updating last login for user: {} at {}", userId, lastLoginAt);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    user.updateLastLogin();
                    return user;
                })
                .flatMap(userRepository::save)
                .then();
    }

    @Override
    public Mono<UserAuthInfo> getUserAuthInfo(Email email) {
        log.debug("Getting auth info for user: {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> UserAuthInfo.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .emailVerified(user.isEmailVerified())
                        .accountStatus(user.getAccountStatus())
                        .build());
    }

    @Override
    @Transactional
    public Mono<User> updateOrderStatistics(UserId userId, Double orderAmount) {
        log.info("Updating order statistics for user: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    int newTotalOrders = user.getTotalOrdersCount() + 1;
                    double newTotalSpent = user.getTotalSpentAmount() + orderAmount;
                    user.updateOrderStatistics(newTotalOrders, newTotalSpent);
                    return user;
                })
                .flatMap(userRepository::save)
                .delayUntil(kafkaEventPort::sendUserOrderStatsUpdatedEvent);
    }

    private Mono<Void> validateUserInput(Email email, String fullName, String phoneNumber) {
        return Mono.fromRunnable(() -> {
            validationService.validateEmail(email);
            validationService.validateFullName(fullName);
            if (phoneNumber != null) {
                validationService.validatePhoneNumber(phoneNumber);
            }
        });
    }
}
