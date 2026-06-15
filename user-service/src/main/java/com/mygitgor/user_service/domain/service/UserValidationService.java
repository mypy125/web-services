package com.mygitgor.user_service.domain.service;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class UserValidationService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9][0-9]{7,14}$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z\\s\\-']{2,100}$");


    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new DomainException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new DomainException("Invalid email format: " + email);
        }
        log.debug("Email validation passed: {}", email);
    }

    public void validateEmail(Email email) {
        if (email == null) {
            throw new DomainException("Email cannot be null");
        }
        validateEmail(email.toString());
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return;
        }
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new DomainException("Invalid phone number format: " + phoneNumber);
        }
        log.debug("Phone number validation passed: {}", phoneNumber);
    }

    public void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainException("Full name cannot be null or empty");
        }
        if (!NAME_PATTERN.matcher(fullName).matches()) {
            throw new DomainException("Invalid full name format: " + fullName);
        }
        log.debug("Full name validation passed: {}", fullName);
    }

    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new DomainException("Password cannot be null or empty");
        }
        if (password.length() < 8) {
            throw new DomainException("Password must be at least 8 characters long");
        }
        if (password.length() > 100) {
            throw new DomainException("Password must be less than 100 characters");
        }
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new DomainException(
                    "Password must contain uppercase, lowercase, digit and special character"
            );
        }
        log.debug("Password validation passed");
    }

    public void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new DomainException("User ID cannot be null or empty");
        }
        try {
            java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid User ID format: " + userId);
        }
        log.debug("User ID validation passed: {}", userId);
    }

    public void validateRole(UserRole role) {
        if (role == null) {
            throw new DomainException("Role cannot be null");
        }
        log.debug("Role validation passed: {}", role);
    }

    public void validateAccountStatus(AccountStatus status) {
        if (status == null) {
            throw new DomainException("Account status cannot be null");
        }
        log.debug("Account status validation passed: {}", status);
    }

    public void validateUser(User user) {
        if (user == null) {
            throw new DomainException("User cannot be null");
        }
        validateEmail(user.getEmail());
        validateFullName(user.getFullName());
        if (user.getPhoneNumber() != null) {
            validatePhoneNumber(user.getPhoneNumber());
        }
        validateRole(user.getRole());
        validateAccountStatus(user.getAccountStatus());
        log.debug("Full user validation passed for: {}", user.getEmail());
    }

    public void validateProfileUpdate(String fullName, String phoneNumber, String profileImage) {
        if (fullName != null && !fullName.isBlank()) {
            validateFullName(fullName);
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            validatePhoneNumber(phoneNumber);
        }
        log.debug("Profile update validation passed");
    }
}
