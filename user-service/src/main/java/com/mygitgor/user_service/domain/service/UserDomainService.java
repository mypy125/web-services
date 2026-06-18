package com.mygitgor.user_service.domain.service;

import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.specification.UserAccountStatusSpec;
import com.mygitgor.user_service.domain.specification.UserEmailUniquenessSpec;
import com.mygitgor.user_service.domain.specification.UserEmailVerifiedSpec;
import com.mygitgor.user_service.domain.specification.UserRoleSpec;
import com.mygitgor.user_service.infrastructure.shared.exception.DomainException;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainService {
    private final UserEmailUniquenessSpec emailUniquenessSpec;
    private final UserAccountStatusSpec accountStatusSpec;
    private final UserEmailVerifiedSpec emailVerifiedSpec;
    private final UserRoleSpec userRoleSpec;

    public Mono<Void> validateEmailUniqueness(Email email) {
        return emailUniquenessSpec.isSatisfiedBy(email)
                .flatMap(isSatisfied -> isSatisfied
                        ? Mono.empty()
                        : Mono.error(new DomainException("Email already exists: " + email)));
    }

    public Mono<Void> validateAccountStatusForLogin(User user) {
        log.debug("Validating account status for login: {}", user.getEmail());
        return accountStatusSpec.isSatisfiedBy(user)
                .flatMap(isSatisfied -> isSatisfied
                        ? Mono.empty()
                        : Mono.error(new DomainException("Account is not active. Status: " + user.getAccountStatus())));
    }

    public Mono<Void> validateEmailVerifiedForSeller(User user) {
        log.debug("Validating email verification for seller: {}", user.getEmail());
        return emailVerifiedSpec.isSatisfiedBy(user)
                .flatMap(isSatisfied -> isSatisfied
                        ? Mono.empty()
                        : Mono.error(new DomainException("Email not verified. Please verify your email before proceeding.")));
    }

    public Mono<Void> validateUserRole(User user, UserRole expectedRole) {
        log.debug("Validating user role: {} for user: {}", expectedRole, user.getEmail());
        return userRoleSpec.isSatisfiedByRole(user, expectedRole)
                .flatMap(isSatisfied -> isSatisfied
                        ? Mono.empty()
                        : Mono.error(new DomainException("User does not have required role: " + expectedRole)));
    }

    public Mono<Void> validateUser(User user) {
        log.debug("Validating user: {}", user.getEmail());

        return validateEmailUniqueness(user.getEmail())
                .then(validateAccountStatusForLogin(user))
                .then(Mono.fromRunnable(() -> {
                    if (user.getFullName() == null || user.getFullName().isBlank()) {
                        throw new DomainException("Full name is required");
                    }
                    if (user.getPhoneNumber() != null && user.getPhoneNumber().length() < 10) {
                        throw new DomainException("Phone number must be at least 10 digits");
                    }
                    log.debug("User validation passed: {}", user.getEmail());
                }));
    }

    public Mono<User> updateUserProfile(User user, String fullName, String phoneNumber, String profileImage) {
        log.debug("Updating profile for user: {}", user.getEmail());

        return Mono.fromCallable(() -> {
            user.updateProfile(fullName, profileImage, phoneNumber);
            return user;
        });
    }

    public Mono<Boolean> canUserPurchase(User user) {
        return accountStatusSpec.isSatisfiedBy(user)
                .flatMap(isActive -> {
                    if (!isActive) {
                        log.warn("User {} cannot purchase - account not active", user.getEmail());
                        return Mono.just(false);
                    }
                    return Mono.just(true);
                });
    }
}
