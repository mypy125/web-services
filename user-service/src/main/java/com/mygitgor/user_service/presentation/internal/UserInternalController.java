package com.mygitgor.user_service.presentation.internal;

import com.mygitgor.user_service.application.service.UserInternalService;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import com.mygitgor.user_service.infrastructure.dto.request.UserAuthInfoDto;
import com.mygitgor.user_service.infrastructure.dto.request.CreateUserRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

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
    public Mono<UserAuthInfoDto> getAuthInfo(@PathVariable String email) {
        log.debug("Internal API: Getting auth info for email: {}", email);
        return userInternalService.getAuthInfo(new Email(email));
    }

    @GetMapping("/id/{userId}")
    public Mono<UserDto> getUserById(@PathVariable String userId) {
        log.debug("Internal API: Getting user by ID: {}", userId);
        return userInternalService.getUserById(new UserId(userId));
    }

    @PostMapping("/")
    public Mono<UserDto> createUser(@RequestBody CreateUserRequest request) {
        log.info("Internal API: Creating user with email: {}", request.getEmail());
        return userInternalService.createUser(request);
    }

    @PutMapping("/{userId}")
    public Mono<UserDto> updateUser(@PathVariable String userId,
                                    @RequestBody UserUpdateRequestDto request) {
        log.info("Internal API: Updating user: {}", userId);
        return userInternalService.updateUser(new UserId(userId), request);
    }

    @PatchMapping("/{email}/verify")
    public Mono<UserDto> verifyEmail(@PathVariable String email) {
        log.info("Internal API: Verifying email for user: {}", email);
        return userInternalService.verifyEmail(new Email(email));
    }

    @PatchMapping("/{email}/last-login")
    public Mono<Void> updateLastLogin(@PathVariable String email,
                                      @RequestBody Map<String, LocalDateTime> body) {
        LocalDateTime lastLoginAt = body.get("lastLoginAt");
        log.debug("Internal API: Updating last login for user: {} at {}", email, lastLoginAt);
        return userInternalService.updateLastLogin(new Email(email), lastLoginAt);
    }

    @GetMapping("/{email}/email-verified")
    public Mono<Boolean> isEmailVerified(@PathVariable String email) {
        log.debug("Internal API: Checking if email is verified: {}", email);
        return userInternalService.isEmailVerified(new Email(email));
    }

    @PatchMapping("/{email}/status")
    public Mono<UserDto> updateAccountStatus(@PathVariable String email,
                                             @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("Internal API: Updating account status for user: {} to {}", email, status);
        return userInternalService.updateAccountStatus(new Email(email), status);
    }

    @PostMapping("/{email}/change-password")
    public Mono<Void> changePassword(@PathVariable String email,
                                     @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        log.info("Internal API: Changing password for user: {}", email);
        return userInternalService.changePassword(new Email(email), newPassword);
    }

    @DeleteMapping("/{email}")
    public Mono<Void> deleteUser(@PathVariable String email) {
        log.info("Internal API: Deleting user: {}", email);
        return userInternalService.deleteUser(new Email(email));
    }

    @GetMapping("/{userId}/statistics")
    public Mono<UserStatisticsDto> getUserStatistics(@PathVariable String userId) {
        log.debug("Internal API: Getting statistics for user: {}", userId);
        return userInternalService.getUserStatistics(new UserId(userId));
    }

    @GetMapping("/search")
    public Mono<Page<UserDto>> searchUsers(@RequestParam String term,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        log.debug("Internal API: Searching users with term: {}", term);
        return userInternalService.searchUsers(term, page, size);
    }
}
