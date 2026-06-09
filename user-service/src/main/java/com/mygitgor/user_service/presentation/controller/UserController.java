package com.mygitgor.user_service.presentation.controller;

import com.mygitgor.user_service.application.service.UserApplicationService;
import com.mygitgor.user_service.domain.model.UserId;
import com.mygitgor.user_service.infrastructure.dto.request.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService userService;

    @GetMapping("/{userId}")
    public Mono<UserDto> getUserById(@PathVariable String userId) {
        log.debug("Getting user by ID: {}", userId);
        return userService.getUserById(new UserId(userId));
    }

    @PutMapping("/{userId}")
    public Mono<UserDto> updateUser(@PathVariable String userId,
                                    @RequestBody UpdateUserRequest request) {
        log.info("Updating user: {}", userId);
        return userService.updateUser(new UserId(userId), request);
    }

    @DeleteMapping("/{userId}")
    public Mono<Void> deleteUser(@PathVariable String userId) {
        log.info("Deleting user: {}", userId);
        return userService.deleteUser(new UserId(userId));
    }

    @PostMapping("/{userId}/verify-email")
    public Mono<UserDto> verifyEmail(@PathVariable String userId) {
        log.info("Verifying email for user: {}", userId);
        return userService.verifyEmail(new UserId(userId));
    }

    @PostMapping("/{userId}/change-password")
    public Mono<Void> changePassword(@PathVariable String userId,
                                     @RequestBody ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);
        return userService.changePassword(new UserId(userId), request.getNewPassword());
    }
}
