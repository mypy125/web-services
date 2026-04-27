package com.mygitgor.auth_service.domain.user.model;

import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.user.event.UserEmailVerifiedEvent;
import com.mygitgor.auth_service.domain.user.event.UserProfileUpdatedEvent;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;

@Getter
@Builder
public class User extends AbstractAggregateRoot<User> {
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

        registerEvent(UserEmailVerifiedEvent.builder()
                .userId(this.id.toString())
                .email(this.email.toString())
                .verifiedAt(this.emailVerifiedAt)
                .build());
    }

    public void updateProfile(String fullName, String profileImage, String phoneNumber) {
        String oldFullName = this.fullName;
        String oldPhoneNumber = this.phoneNumber;

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

        registerEvent(UserProfileUpdatedEvent.builder()
                .userId(this.id.toString())
                .oldFullName(oldFullName)
                .newFullName(this.fullName)
                .oldPhoneNumber(oldPhoneNumber)
                .newPhoneNumber(this.phoneNumber)
                .updatedAt(this.updatedAt)
                .build());
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void suspend() {
        if (this.accountStatus == AccountStatus.SUSPENDED) {
            throw new DomainException("User already suspended");
        }
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Cannot suspend banned user");
        }
        this.accountStatus = AccountStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.accountStatus == AccountStatus.ACTIVE) {
            throw new DomainException("User already active");
        }
        this.accountStatus = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void ban() {
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("User already banned");
        }
        this.accountStatus = AccountStatus.BANNED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }

    public boolean canLogin() {
        return isActive() && (emailVerified || role == UserRole.ROLE_ADMIN);
    }

    public static User register(Email email, String fullName, UserRole role) {
        return User.builder()
                .id(new UserId())
                .email(email)
                .fullName(fullName)
                .role(role)
                .emailVerified(false)
                .accountStatus(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
