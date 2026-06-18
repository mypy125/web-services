package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.repository.UserRepositoryPort;
import com.mygitgor.user_service.domain.service.UserDomainService;
import com.mygitgor.user_service.infrastructure.cache.UserCacheService;
import com.mygitgor.user_service.infrastructure.dto.request.*;
import com.mygitgor.user_service.infrastructure.dto.response.UserAuthInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserStatisticsResponse;
import com.mygitgor.user_service.infrastructure.mapper.UserMapper;
import com.mygitgor.user_service.infrastructure.mapper.UserStatisticsMapper;
import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;
import com.mygitgor.user_service.infrastructure.shared.exception.UserNotFoundException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Page;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInternalService {
    private final UserRepositoryPort userRepository;
    private final UserDomainService userDomainService;
    private final UserStatisticsMapper userStatisticsMapper;
    private final UserCacheService cacheService;
    private final UserMapper userMapper;

    public Mono<Boolean> existsByEmail(Email email) {
        return cacheService.getCachedUserByEmail(email)
                .map(user -> true)
                .switchIfEmpty(userRepository.existsByEmail(email));
    }

    public Mono<UserAuthInfoResponse> getAuthInfo(Email email) {
        return cacheService.getCachedAuthInfo(email.value())
                .map(map -> {
                    String fullName = map.containsKey("fullName") ? (String) map.get("fullName") : "";

                    return new UserAuthInfoResponse(
                            (String) map.get("userId"),
                            (String) map.get("email"),
                            fullName,
                            UserRole.valueOf((String) map.get("role")).name(),
                            (Boolean) map.get("emailVerified"),
                            (String) map.get("accountStatus")
                    );
                })
                .switchIfEmpty(Mono.defer(() -> userRepository.findByEmail(email)
                        .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                        .flatMap(user -> cacheService.cacheAuthInfo(
                                user.getEmail().value(),
                                user.getId().getValue().toString(),
                                user.getRole().name(),
                                user.isEmailVerified(),
                                user.getAccountStatus().name()
                        ).thenReturn(user))
                        .map(userMapper::toAuthInfoResponse)
                ));
    }

    public Mono<UserResponse> getUserById(UserId userId) {
        return cacheService.getCachedUserById(userId)
                .switchIfEmpty(Mono.defer(() -> userRepository.findById(userId)
                        .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                        .flatMap(user -> cacheService.cacheUserById(user).thenReturn(user))
                ))
                .map(userMapper::toResponse);
    }

    public Mono<UserResponse> getUserByEmail(Email email) {
        log.debug("Getting user by email: {}", email);
        return cacheService.getCachedUserByEmail(email)
                .switchIfEmpty(Mono.defer(() -> userRepository.findByEmail(email)
                        .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + email)))
                        .flatMap(user -> cacheService.cacheUserByEmail(user).thenReturn(user))
                ))
                .map(userMapper::toResponse);
    }

    public Mono<Boolean> isEmailVerified(Email email) {
        log.debug("Checking if email is verified: {}", email);
        return cacheService.getCachedUserByEmail(email)
                .map(User::isEmailVerified)
                .switchIfEmpty(Mono.defer(() -> userRepository.findByEmail(email)
                        .map(User::isEmailVerified)
                        .defaultIfEmpty(false)));
    }

    public Mono<UserStatisticsResponse> getUserStatistics(UserId userId) {
        log.debug("Getting statistics for user: {}", userId);
        return cacheService.getCachedStatistics(userId)
                .map(map -> (UserStatisticsResponse) map.get("statsResponse"))
                .switchIfEmpty(Mono.defer(() -> userRepository.getStatistics(userId)
                        .map(userStatisticsMapper::toResponse)
                        .flatMap(response -> {
                            Map<String, Object> cacheMap = Map.of("statsResponse", response);
                            return cacheService.cacheStatistics(userId, cacheMap).thenReturn(response);
                        })
                ));
    }

    public Mono<Page<UserResponse>> searchUsers(String searchTerm, int page, int size) {
        log.debug("Searching users with term: {}", searchTerm);
        return cacheService.getCachedUserList(searchTerm, page, size)
                .switchIfEmpty(Mono.defer(() -> userRepository.search(searchTerm, page, size)
                        .map(userPage -> userPage.map(userMapper::toResponse))
                        .flatMap(pageResponse -> cacheService.cacheUserList(searchTerm, page, size, pageResponse)
                                .thenReturn(pageResponse))
                ));
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
                .flatMap(user -> cacheService.refreshUserCache(user).thenReturn(user))
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
                .flatMap(user -> cacheService.refreshUserCache(user).thenReturn(user))
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
                .flatMap(user -> cacheService.refreshUserCache(user).thenReturn(user))
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
                .flatMap(cacheService::refreshUserCache)
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
                .flatMap(user -> cacheService.refreshUserCache(user).thenReturn(user))
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
                .flatMap(user -> userRepository.deleteByEmail(email)
                        .then(cacheService.evictAllUserCaches(user)));
    }

}
