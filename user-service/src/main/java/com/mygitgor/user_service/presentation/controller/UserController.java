package com.mygitgor.user_service.presentation.controller;

import com.mygitgor.user_service.application.service.UserApplicationService;
import com.mygitgor.user_service.infrastructure.dto.request.ChangePasswordRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateProfileRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateUserRequest;
import com.mygitgor.user_service.infrastructure.dto.response.UserResponse;
import com.mygitgor.user_service.infrastructure.mapper.UserMapper;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Management", description = "Endpoints for managing user profiles")
public class UserController {
    private final UserApplicationService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile",
            description = "Fetches the profile details of the user extracted from the JWT token."
    )
    public Mono<UserResponse> getCurrentUser(Authentication authentication) {
        log.debug("Getting current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.getUserById(new UserId(userId))
                .map(userMapper::toResponse);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile by ID",
            description = "Fetches public or internal profile information for a specific user ID."
    )
    public Mono<UserResponse> getUserById(@PathVariable String userId) {
        log.debug("Getting user by ID: {}", userId);
        return userService.getUserById(new UserId(userId))
                .map(userMapper::toResponse);
    }


    @PutMapping("/me")
    @Operation(summary = "Update current user profile",
            description = "Allows the authenticated user to update personal details like name and phone number."
    )
    public Mono<UserResponse> updateCurrentUser(Authentication authentication,
                                                @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Updating current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.updateProfile(new UserId(userId), request)
                .map(userMapper::toResponse);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update any user profile (Admin only)",
            description = "Administrative endpoint to update user profiles, modify roles, or change account statuses."
    )
    public Mono<UserResponse> updateUser(@PathVariable String userId,
                                         @Valid @RequestBody UpdateUserRequest request) {
        log.info("Admin: Updating user: {}", userId);
        return userService.updateUser(new UserId(userId), request)
                .map(userMapper::toResponse);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete or deactivate own account",
            description = "Allows the current user to request account deletion or permanent deactivation."
    )
    public Mono<Void> deleteCurrentUser(Authentication authentication) {
        log.info("Deleting current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.deleteUser(new UserId(userId));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user by ID (Admin only)",
            description = "Administrative endpoint to soft-delete or hard-delete a user account from the platform."
    )
    public Mono<Void> deleteUser(@PathVariable String userId) {
        log.info("Admin: Deleting user: {}", userId);
        return userService.deleteUser(new UserId(userId));
    }

    @PostMapping("/me/verify-email")
    @Operation(summary = "Trigger email verification process",
            description = "Initiates the email confirmation workflow. Sends a verification link or OTP via Notification Service."
    )
    public Mono<UserResponse> verifyEmail(Authentication authentication) {
        log.info("Verifying email for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.verifyEmail(new UserId(userId))
                .map(userMapper::toResponse);
    }

    @PostMapping("/me/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change current user password",
            description = "Validates the old password and sets up a new secure password for the authenticated account."
    )
    public Mono<Void> changePassword(Authentication authentication,
                                     @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Changing password for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.changePassword(new UserId(userId), request.getNewPassword());
    }

    @PostMapping("/me/profile-image")
    @Operation(summary = "Upload or update profile avatar",
            description = "Accepts a multipart image file to upload and associate as the user's profile picture."
    )
    public Mono<UserResponse> uploadProfileImage(Authentication authentication,
                                                 @RequestPart("image") Mono<FilePart> filePart) {
        log.info("Uploading profile image for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.uploadProfileImage(new UserId(userId), filePart)
                .map(userMapper::toResponse);
    }

    @DeleteMapping("/me/profile-image")
    public Mono<UserResponse> deleteProfileImage(Authentication authentication) {
        log.info("Deleting profile image for current user");
        String userId = ((AuthUser) authentication.getPrincipal()).getUserId();
        return userService.deleteProfileImage(new UserId(userId))
                .map(userMapper::toResponse);
    }
}
