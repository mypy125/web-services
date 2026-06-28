package com.mygitgor.user_service.domain.model;

import com.mygitgor.user_service.shared.exception.DomainException;
import com.mygitgor.user_service.shared.valueobject.Email;
import com.mygitgor.user_service.shared.valueobject.UserId;
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
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime emailVerifiedAt;

    // TODO: Default address and payment settings
    private String defaultAddressId;
    private String defaultPaymentMethodId;
    private String defaultShippingAddressId;

    // TODO: Aggregated order statistics
    private Integer totalOrdersCount;
    private Double totalSpentAmount;

    // TODO: Localization and Marketing
    private String language;
    private String timezone;
    private boolean newsletterSubscribed;
    private boolean marketingConsent;

    // TODO: Safety
    private LocalDateTime lastPasswordChangeAt;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;

    public static User register(Email email, String fullName, String phoneNumber, UserRole role) {
        if (email == null) {
            throw new DomainException("Email is required for registration");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new DomainException("Full name is required for registration");
        }

        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .id(new UserId())
                .email(email)
                .fullName(fullName.trim())
                .phoneNumber(phoneNumber)
                .role(role != null ? role : UserRole.ROLE_CUSTOMER)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)

                .totalOrdersCount(0)
                .totalSpentAmount(0.0)
                .failedLoginAttempts(0)

                .language("en")
                .timezone("UTC")
                .newsletterSubscribed(false)
                .marketingConsent(false)

                .createdAt(now)
                .updatedAt(now)
                .lastPasswordChangeAt(now)
                .build();
    }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void registerFailedLoginAttempt(int maxAttempts, int lockDurationMinutes) {
        if (this.failedLoginAttempts == null) {
            this.failedLoginAttempts = 0;
        }
        this.failedLoginAttempts++;
        this.updatedAt = LocalDateTime.now();

        if (this.failedLoginAttempts >= maxAttempts) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String hashedNewPassword) {
        this.lastPasswordChangeAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canLogin() {
        if (this.accountStatus == null) return false;
        return !this.accountStatus.isBlocked() && !isLocked();
    }

    public void verifyEmail() {
        if (this.emailVerified) {
            throw new DomainException("Email already verified");
        }
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
        if (this.accountStatus == AccountStatus.PENDING_VERIFICATION) {
            this.accountStatus = AccountStatus.ACTIVE;
        }
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

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.resetFailedLoginAttempts();
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
            throw new DomainException("Only active users can be suspended. Current status: " + this.accountStatus);
        }
        this.accountStatus = AccountStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.accountStatus == AccountStatus.ACTIVE) {
            throw new DomainException("User is already active");
        }
        this.accountStatus = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.accountStatus != null && this.accountStatus.isActive();
    }

    public void updateRole(UserRole newRole) {
        if (newRole == null) {
            throw new DomainException("New role cannot be null");
        }
        if (this.role == newRole) {
            throw new DomainException("User already has the role: " + newRole);
        }
        if (this.role == UserRole.ROLE_ADMIN && newRole != UserRole.ROLE_ADMIN) {
            throw new DomainException("Cannot downgrade an ADMIN role via standard user update");
        }
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Cannot change role for a banned user");
        }
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateOrderStatistics(Integer newTotalOrders, Double newTotalSpent) {
        if (newTotalOrders == null || newTotalSpent == null) {
            throw new DomainException("Order statistics values cannot be null");
        }
        if (newTotalOrders < this.getTotalOrdersCount()) {
            throw new DomainException("New total orders count cannot be less than current count");
        }
        if (newTotalSpent < this.getTotalSpentAmount()) {
            throw new DomainException("New total spent amount cannot be less than current amount");
        }
        if (newTotalOrders < 0 || newTotalSpent < 0) {
            throw new DomainException("Order statistics cannot have negative values");
        }

        this.totalOrdersCount = newTotalOrders;
        this.totalSpentAmount = newTotalSpent;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts != null ? failedLoginAttempts : 0;
    }

    public Integer getTotalOrdersCount() {
        return totalOrdersCount != null ? totalOrdersCount : 0;
    }

    public Double getTotalSpentAmount() {
        return totalSpentAmount != null ? totalSpentAmount : 0.0;
    }

    public boolean canPurchase() {
        return isActive() && emailVerified;
    }

    public boolean needsEmailVerification() {
        return !emailVerified && role != UserRole.ROLE_ADMIN;
    }
}
