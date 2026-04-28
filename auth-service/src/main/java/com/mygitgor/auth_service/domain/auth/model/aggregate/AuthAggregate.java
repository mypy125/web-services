package com.mygitgor.auth_service.domain.auth.model.aggregate;

import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.event.*;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.domain.user.event.UserLoggedInEvent;
import com.mygitgor.auth_service.domain.user.event.UserRegisteredEvent;
import com.mygitgor.auth_service.domain.user.event.UserRoleChangedEvent;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class AuthAggregate extends AbstractAggregateRoot<AuthAggregate> {
    private Email email;
    private UserId userId;
    private UserRole role;
    private AccountStatus accountStatus;
    private Token currentToken;
    private List<Token> tokenHistory;
    private List<VerificationCode> verificationCodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime emailVerifiedAt;
    private int failedLoginAttempts;
    private boolean locked;
    private LocalDateTime lockedUntil;
    private String deviceId;
    private String ipAddress;
    private String userAgent;

    private AuthAggregate() {
        this.tokenHistory = new ArrayList<>();
        this.verificationCodes = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.locked = false;
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public static AuthAggregate register(Email email, UserId userId, UserRole role, String deviceId, String ipAddress, String userAgent) {
        AuthAggregate aggregate = new AuthAggregate();
        aggregate.email = email;
        aggregate.userId = userId;
        aggregate.role = role;
        aggregate.deviceId = deviceId;
        aggregate.ipAddress = ipAddress;
        aggregate.userAgent = userAgent;

        aggregate.registerEvent(UserRegisteredEvent.builder()
                .email(email.toString())
                .userId(userId.toString())
                .role(role)
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .occurredAt(LocalDateTime.now())
                .build());

        return aggregate;
    }

    public Token login(Token newToken, String deviceId, String ipAddress, String userAgent) {
        if (locked) {
            if (lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) {
                throw new DomainException("Account is locked. Try again later.");
            } else {
                this.locked = false;
                this.failedLoginAttempts = 0;
                this.lockedUntil = null;
            }
        }

        if (accountStatus != AccountStatus.ACTIVE) {
            throw new DomainException("Account is not active. Status: " + accountStatus);
        }

        if (role == UserRole.ROLE_SELLER && emailVerifiedAt == null) {
            throw new DomainException("Email not verified. Please verify your email before logging in.");
        }

        if (this.currentToken != null && this.currentToken.isValid()) {
            this.currentToken.blacklist();
            tokenHistory.add(this.currentToken);

            registerEvent(TokenBlacklistedEvent.builder()
                    .token(this.currentToken.getValue().toString())
                    .email(email.toString())
                    .reason("New login from different device")
                    .occurredAt(LocalDateTime.now())
                    .build());
        }

        this.currentToken = newToken;
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.failedLoginAttempts = 0;

        registerEvent(UserLoggedInEvent.builder()
                .email(email.toString())
                .userId(userId.toString())
                .token(newToken.getValue().toString())
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .occurredAt(LocalDateTime.now())
                .build());

        return newToken;
    }

    public AuthAggregate loginWithoutToken(String deviceId, String ipAddress, String userAgent) {
        if (locked) {
            if (lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) {
                throw new DomainException("Account is locked. Try again later.");
            }
        }

        if (accountStatus != AccountStatus.ACTIVE) {
            throw new DomainException("Account is not active. Status: " + accountStatus);
        }

        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.failedLoginAttempts = 0;

        return this;
    }

    public void logout(String tokenValue, String reason) {
        if (currentToken != null && currentToken.getValue().toString().equals(tokenValue)) {
            currentToken.blacklist();
            tokenHistory.add(currentToken);
            this.currentToken = null;

            registerEvent(TokenBlacklistedEvent.builder()
                    .token(tokenValue)
                    .email(email.toString())
                    .reason(reason)
                    .occurredAt(LocalDateTime.now())
                    .build());
        }
    }

    public void logoutAllDevices() {
        if (currentToken != null) {
            currentToken.blacklist();
            tokenHistory.add(currentToken);
            this.currentToken = null;
        }

        tokenHistory.forEach(token -> {
            if (token.isValid()) {
                token.blacklist();
            }
        });

        registerEvent(AllDevicesLoggedOutEvent.builder()
                .email(email.toString())
                .userId(userId.toString())
                .occurredAt(LocalDateTime.now())
                .build());
    }

    public void recordFailedLoginAttempt() {
        this.failedLoginAttempts++;
        this.updatedAt = LocalDateTime.now();

        final int MAX_FAILED_ATTEMPTS = 5;
        final int LOCK_DURATION_MINUTES = 30;

        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.locked = true;
            this.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);

            registerEvent(AccountLockedEvent.builder()
                    .email(email.toString())
                    .reason("Too many failed login attempts")
                    .lockedUntil(lockedUntil)
                    .occurredAt(LocalDateTime.now())
                    .build());
        }
    }

    public void verifyEmail() {
        if (emailVerifiedAt != null) {
            throw new DomainException("Email already verified");
        }

        this.emailVerifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        registerEvent(EmailVerifiedEvent.builder()
                .email(email.toString())
                .userId(userId.toString())
                .verifiedAt(emailVerifiedAt)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    public void addVerificationCode(VerificationCode code) {
        verificationCodes.removeIf(existing ->
                existing.getPurpose() == code.getPurpose() &&
                        existing.isValid() &&
                        existing.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5))
        );

        verificationCodes.add(code);

        registerEvent(OtpGeneratedEvent.builder()
                .email(code.getEmail().toString())
                .otp(code.getOtp().getValue())
                .purpose(code.getPurpose())
                .expiresAt(code.getOtp().getExpiresAt())
                .occurredAt(LocalDateTime.now())
                .build());
    }

    public VerificationCode verifyOtp(String otpValue, OtpPurpose purpose,
                                      String deviceId, String ipAddress, String userAgent) {
        VerificationCode validCode = verificationCodes.stream()
                .filter(code -> code.getOtp().getValue().equals(otpValue))
                .filter(code -> code.getPurpose() == purpose)
                .filter(VerificationCode::isValid)
                .findFirst()
                .orElseThrow(() -> {
                    registerEvent(OtpVerifiedEvent.failure(
                            this,
                            otpValue,
                            email.toString(),
                            userId.toString(),
                            role,
                            purpose,
                            deviceId,
                            ipAddress,
                            userAgent,
                            "Invalid or expired OTP",
                            getRemainingAttempts(otpValue, purpose)
                    ));
                    return new DomainException("Invalid or expired OTP");
                });

        validCode.markAsUsed();
        this.updatedAt = LocalDateTime.now();

        registerEvent(OtpVerifiedEvent.success(
                this,
                otpValue,
                email.toString(),
                userId.toString(),
                role,
                purpose,
                deviceId,
                ipAddress,
                userAgent,
                LocalDateTime.now()
        ));

        cleanupExpiredOtps();

        if (purpose == OtpPurpose.EMAIL_VERIFICATION || purpose == OtpPurpose.REGISTRATION) {
            verifyEmail();
        }

        return validCode;
    }

    private int getRemainingAttempts(String otpValue, OtpPurpose purpose) {
        long attempts = verificationCodes.stream()
                .filter(code -> code.getPurpose() == purpose)
                .filter(code -> !code.getOtp().getValue().equals(otpValue))
                .filter(code -> code.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5)))
                .count();
        return 3 - (int) attempts;
    }

    public void cleanupExpiredOtps() {
        verificationCodes.removeIf(code -> !code.isValid());
    }

    public Optional<VerificationCode> getValidOtp(OtpPurpose purpose) {
        return verificationCodes.stream()
                .filter(code -> code.getPurpose() == purpose)
                .filter(VerificationCode::isValid)
                .findFirst();
    }

    public void updateAccountStatus(AccountStatus newStatus, String reason) {
        AccountStatus oldStatus = this.accountStatus;
        this.accountStatus = newStatus;
        this.updatedAt = LocalDateTime.now();

        registerEvent(AccountStatusChangedEvent.builder()
                .email(email.toString())
                .userId(userId.toString())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    public void suspend(String reason) {
        if (accountStatus == AccountStatus.SUSPENDED) {
            throw new DomainException("Account already suspended");
        }
        if (accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Cannot suspend a banned account");
        }

        updateAccountStatus(AccountStatus.SUSPENDED, reason);

        if (currentToken != null) {
            logout(currentToken.getValue().toString(), "Account suspended: " + reason);
        }
    }

    public void activate() {
        if (accountStatus == AccountStatus.ACTIVE) {
            throw new DomainException("Account already active");
        }
        updateAccountStatus(AccountStatus.ACTIVE, "Account activated");
    }

    public void ban(String reason) {
        if (accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Account already banned");
        }

        updateAccountStatus(AccountStatus.BANNED, reason);

        if (currentToken != null) {
            logout(currentToken.getValue().toString(), "Account banned: " + reason);
        }
    }

    public void updateRole(UserRole newRole, String updatedBy) {
        UserRole oldRole = this.role;
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();

        registerEvent(UserRoleChangedEvent.builder()
                .email(email.toString())
                .userId(userId.toString())
                .oldRole(oldRole)
                .newRole(newRole)
                .updatedBy(updatedBy)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    public Token refreshToken(Token newToken) {
        if (currentToken != null) {
            logout(currentToken.getValue().toString(), "Token refreshed");
        }

        this.currentToken = newToken;
        this.updatedAt = LocalDateTime.now();

        return newToken;
    }

    public boolean canLogin() {
        return accountStatus == AccountStatus.ACTIVE
                && !locked
                && (role != UserRole.ROLE_SELLER || emailVerifiedAt != null);
    }

    public boolean isActive() {
        return accountStatus == AccountStatus.ACTIVE;
    }

    public List<Token> getTokenHistory() {
        return new ArrayList<>(tokenHistory);
    }

    public List<Token> getActiveTokens() {
        List<Token> activeTokens = new ArrayList<>();
        if (currentToken != null && currentToken.isValid()) {
            activeTokens.add(currentToken);
        }
        activeTokens.addAll(tokenHistory.stream()
                .filter(Token::isValid)
                .collect(Collectors.toList()));
        return activeTokens;
    }

    public void clearVerificationCodes() {
        verificationCodes.clear();
    }

    public void updateDeviceInfo(String deviceId, String ipAddress, String userAgent) {
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("AuthAggregate{email=%s, userId=%s, role=%s, status=%s, currentToken=%s}",
                email, userId, role, accountStatus, currentToken != null);
    }
}
