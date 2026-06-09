package com.mygitgor.user_service.domain.model;

import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class User {
    private final UserId id;
    private final Email email;
    private String fullName;
    private UserRole role;
    private boolean emailVerified;
    private String profileImage;
    private String phoneNumber;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime emailVerifiedAt;

    public void verifyEmail() {
        if (this.emailVerified) {
            throw new DomainException("Email already verified");
        }
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String fullName, String profileImage, String phoneNumber) {
        if (fullName != null && !fullName.isBlank()) {
            this.fullName = fullName;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }
        this.updatedAt = LocalDateTime.now();
    }
}
