package com.mygitgor.user_service.presentation.internal;

import com.mygitgor.user_service.application.service.UserInternalService;
import com.mygitgor.user_service.infrastructure.dto.request.*;
import com.mygitgor.user_service.infrastructure.dto.response.UserAuthInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserStatisticsResponse;
import com.mygitgor.user_service.infrastructure.mapper.UserMapper;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {
    private final UserInternalService userInternalService;

    @GetMapping("/exists/{email}")
    public Mono<Boolean> existsByEmail(@PathVariable String email) {
        log.debug("Internal API: Checking existence for email: {}", email);
        return userInternalService.existsByEmail(new Email(email));
    }

    @GetMapping("/{email}/auth-info")
    public Mono<UserAuthInfoResponse> getAuthInfo(@PathVariable String email) {
        log.debug("Internal API: Getting auth info for email: {}", email);
        return userInternalService.getAuthInfo(new Email(email));
    }

    @GetMapping("/id/{userId}")
    public Mono<UserResponse> getUserById(@PathVariable String userId) {
        log.debug("Internal API: Getting user by ID: {}", userId);
        return userInternalService.getUserById(new UserId(userId));
    }

    @GetMapping("/{email}")
    public Mono<UserResponse> getUserByEmail(@PathVariable String email) {
        log.debug("Internal API: Getting user by email: {}", email);
        return userInternalService.getUserByEmail(new Email(email));
    }

    @GetMapping("/{email}/email-verified")
    public Mono<Boolean> isEmailVerified(@PathVariable String email) {
        log.debug("Internal API: Checking if email is verified: {}", email);
        return userInternalService.isEmailVerified(new Email(email));
    }

    @GetMapping("/{userId}/statistics")
    public Mono<UserStatisticsResponse> getUserStatistics(@PathVariable String userId) {
        log.debug("Internal API: Getting statistics for user: {}", userId);
        return userInternalService.getUserStatistics(new UserId(userId));
    }

    @GetMapping("/search")
    public Mono<Page<UserResponse>> searchUsers(@RequestParam String term,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("Internal API: Searching users with term: {}", term);
        return userInternalService.searchUsers(term, page, size);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Internal API: Creating user with email: {}", request.getEmail());
        return userInternalService.createUser(request);
    }

    @PutMapping("/{userId}")
    public Mono<UserResponse> updateUser(@PathVariable String userId,
                                         @Valid @RequestBody UpdateUserRequest request) {
        log.info("Internal API: Updating user: {}", userId);
        return userInternalService.updateUser(new UserId(userId), request);
    }

    @PatchMapping("/{email}/verify")
    public Mono<UserResponse> verifyEmail(@PathVariable String email) {
        log.info("Internal API: Verifying email for user: {}", email);
        return userInternalService.verifyEmail(new Email(email));
    }

    @PatchMapping("/{email}/last-login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateLastLogin(@PathVariable String email,
                                      @Valid @RequestBody UpdateLastLoginRequest request) {
        log.debug("Internal API: Updating last login for user: {} at {}", email, request.getLastLoginAt());
        return userInternalService.updateLastLogin(new Email(email), request.getLastLoginAt());
    }

    @PatchMapping("/{email}/status")
    public Mono<UserResponse> updateAccountStatus(@PathVariable String email,
                                                  @Valid @RequestBody UpdateAccountStatusRequest request) {
        log.info("Internal API: Updating account status for user: {} to {}", email, request.getStatus());
        return userInternalService.updateAccountStatus(new Email(email), request);
    }

    @PostMapping("/{email}/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> changePassword(@PathVariable String email,
                                     @Valid @RequestBody ChangePasswordInternalRequest request) {
        log.info("Internal API: Changing password for user: {}", email);
        return userInternalService.changePassword(new Email(email), request.getNewPassword());
    }

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable String email) {
        log.info("Internal API: Deleting user: {}", email);
        return userInternalService.deleteUser(new Email(email));
    }

    @GetMapping("/batch")
    public Mono<List<UserResponse>> getUsersByIds(@RequestParam List<String> userIds) {
        log.debug("Internal API: Getting users by IDs: {}", userIds);
        List<UserId> domainIds = userIds.stream()
                .map(UserId::new)
                .collect(Collectors.toList());
        return userInternalService.getUsersByIds(domainIds);
    }
}
