package com.mygitgor.user_service.presentation.controller;

import com.mygitgor.user_service.application.service.UserApplicationService;
import com.mygitgor.user_service.infrastructure.dto.request.ChangePasswordRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateProfileRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateUserRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UserDto;
import com.mygitgor.user_service.infrastructure.dto.response.UserResponse;
import com.mygitgor.user_service.infrastructure.mapper.UserDtoMapper;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService userService;
    private final UserDtoMapper userDtoMapper;

    @GetMapping("/me")
    public Mono<UserResponse> getCurrentUser(Authentication authentication) {
        log.debug("Getting current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.getUserById(new UserId(userId))
                .map(userDtoMapper::toResponse);
    }

    @GetMapping("/{userId}")
    public Mono<UserResponse> getUserById(@PathVariable String userId) {
        log.debug("Getting user by ID: {}", userId);
        return userService.getUserById(new UserId(userId))
                .map(userDtoMapper::toResponse);
    }


    @PutMapping("/me")
    public Mono<UserResponse> updateCurrentUser(Authentication authentication,
                                                @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Updating current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.updateProfile(new UserId(userId), request)
                .map(userDtoMapper::toResponse);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserResponse> updateUser(@PathVariable String userId,
                                         @Valid @RequestBody UpdateUserRequest request) {
        log.info("Admin: Updating user: {}", userId);
        return userService.updateUser(new UserId(userId), request)
                .map(userDtoMapper::toResponse);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCurrentUser(Authentication authentication) {
        log.info("Deleting current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.deleteUser(new UserId(userId));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteUser(@PathVariable String userId) {
        log.info("Admin: Deleting user: {}", userId);
        return userService.deleteUser(new UserId(userId));
    }

    @PostMapping("/me/verify-email")
    public Mono<UserResponse> verifyEmail(Authentication authentication) {
        log.info("Verifying email for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.verifyEmail(new UserId(userId))
                .map(userDtoMapper::toResponse);
    }

    @PostMapping("/me/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> changePassword(Authentication authentication,
                                     @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Changing password for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.changePassword(new UserId(userId), request.getNewPassword());
    }

    @PostMapping("/me/profile-image")
    public Mono<UserResponse> uploadProfileImage(Authentication authentication,
                                                 @RequestPart("image") Mono<FilePart> filePart) {
        log.info("Uploading profile image for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.uploadProfileImage(new UserId(userId), filePart)
                .map(userDtoMapper::toResponse);
    }

    @DeleteMapping("/me/profile-image")
    public Mono<UserResponse> deleteProfileImage(Authentication authentication) {
        log.info("Deleting profile image for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.deleteProfileImage(new UserId(userId))
                .map(userDtoMapper::toResponse);
    }
}
