package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.repository.UserRepositoryPort;
import com.mygitgor.user_service.domain.service.UserDomainService;
import com.mygitgor.user_service.infrastructure.dto.request.*;
import com.mygitgor.user_service.infrastructure.dto.response.UserAuthInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserStatisticsResponse;
import com.mygitgor.user_service.infrastructure.mapper.UserMapper;
import com.mygitgor.user_service.infrastructure.mapper.UserStatisticsMapper;
import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;
import com.mygitgor.user_service.infrastructure.shared.exception.UserNotFoundException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInternalService {
    private final UserRepositoryPort userRepository;
    private final UserDomainService userDomainService;
    private final UserStatisticsMapper userStatisticsMapper;
    private final UserMapper userMapper;

    public Mono<Boolean> existsByEmail(Email email) {
        return userRepository.existsByEmail(email);
    }

    public Mono<UserAuthInfoResponse> getAuthInfo(Email email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(userMapper::toAuthInfoResponse);
    }

    public Mono<UserResponse> getUserById(UserId userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(userMapper::toResponse);
    }

    public Mono<UserResponse> getUserByEmail(Email email) {
        log.debug("Getting user by email: {}", email);
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(userMapper::toResponse);
    }

    public Mono<Boolean> isEmailVerified(Email email) {
        log.debug("Checking if email is verified: {}", email);
        return userRepository.findByEmail(email)
                .map(User::isEmailVerified)
                .defaultIfEmpty(false);
    }

    public Mono<UserStatisticsResponse> getUserStatistics(UserId userId) {
        log.debug("Getting statistics for user: {}", userId);
        return userRepository.getStatistics(userId)
                .map(userStatisticsMapper::toResponse);
    }

    public Mono<Page<UserResponse>> searchUsers(String searchTerm, int page, int size) {
        log.debug("Searching users with term: {}", searchTerm);
        return userRepository.search(searchTerm, page, size)
                .map(userPage -> userPage.map(userMapper::toResponse));
    }

    public Mono<List<UserResponse>> getUsersByIds(List<UserId> userIds) {
        log.debug("Getting users by IDs: {}", userIds);
        return userRepository.findByIds(userIds)
                .map(userMapper::toResponse)
                .collectList();
    }

    public Mono<UserResponse> createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());

        return userDomainService.validateEmailUniqueness(new Email(request.email()))
                .then(Mono.fromCallable(() -> User.register(
                        new Email(request.email()),
                        request.fullName(),
                        request.phoneNumber(),
                        request.role() != null ? UserRole.valueOf(request.role()) : UserRole.ROLE_CUSTOMER
                )))
                .flatMap(userRepository::save)
                .map(userMapper::toResponse)
                .doOnSuccess(user -> log.info("User created successfully: {}", request.email()))
                .doOnError(error -> log.error("Failed to create user: {}", request.email(), error));
    }

    public Mono<UserResponse> updateUser(UserId userId, UpdateUserRequest request) {
        log.info("Updating user: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                .map(user -> {
                    userMapper.updateDomain(user, request);
                    return user;
                })
                .flatMap(userRepository::save)
                .map(userMapper::toResponse);
    }

    public Mono<UserResponse> verifyEmail(Email email) {
        log.info("Verifying email for user: {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {
                    user.verifyEmail();
                    return user;
                })
                .flatMap(userRepository::save)
                .map(userMapper::toResponse);
    }

    public Mono<Void> updateLastLogin(Email email, LocalDateTime lastLoginAt) {
        log.debug("Updating last login for user: {} at {}", email, lastLoginAt);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {
                    user.updateLastLogin();
                    return user;
                })
                .flatMap(userRepository::save)
                .then();
    }

    public Mono<UserResponse> updateAccountStatus(Email email, UpdateAccountStatusRequest request) {
        log.info("Updating account status for user: {} to {}", email, request.status());

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .map(user -> {
                    AccountStatus status = AccountStatus.valueOf(request.status());
                    switch (status) {
                        case BANNED -> user.ban();
                        case SUSPENDED -> user.suspend();
                        case ACTIVE -> user.activate();
                        default -> throw new DomainException("Unknown status: " + status);
                    }
                    return user;
                })
                .flatMap(userRepository::save)
                .map(userMapper::toResponse);
    }

    public Mono<Void> changePassword(Email email, String newPassword) {
        log.info("Changing password for user: {}", email);
        return Mono.empty();
    }

    public Mono<Void> deleteUser(Email email) {
        log.info("Deleting user: {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                .flatMap(user -> userRepository.deleteByEmail(email));
    }

}
