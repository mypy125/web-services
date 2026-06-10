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

    public static User register(Email email, String fullName, UserRole role) {
        if (email == null) {
            throw new DomainException("Email is required for registration");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new DomainException("Full name is required for registration");
        }

        return User.builder()
                .id(new UserId())
                .email(email)
                .fullName(fullName.trim())
                .role(role != null ? role : UserRole.ROLE_CUSTOMER)
                .accountStatus(AccountStatus.PENDING)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void ban() {
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("User is already banned");
        }

        this.accountStatus = AccountStatus.BANNED;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        if (this.accountStatus != AccountStatus.ACTIVE) {
            throw new DomainException("Only active users can be suspended");
        }

        this.accountStatus = AccountStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.accountStatus == AccountStatus.ACTIVE) {
            throw new DomainException("User is already active");
        }

        if (!this.emailVerified) {
            this.emailVerified = true;
            this.emailVerifiedAt = LocalDateTime.now();
        }

        this.accountStatus = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
}
