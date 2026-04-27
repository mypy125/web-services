package com.mygitgor.auth_service.domain.user.port;

import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.domain.user.model.User;
import com.mygitgor.auth_service.domain.user.model.UserStatistics;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface UserPort {
    Mono<Boolean> existsByEmail(Email email);
    Mono<User> getUserByEmail(Email email);
    Mono<User> getUserById(UserId userId);
    Mono<User> createUser(User user);
    Mono<User> updateUser(User user);
    Mono<User> verifyEmail(Email email);
    Mono<Void> updateLastLogin(Email email, LocalDateTime lastLoginAt);
    Mono<Boolean> isEmailVerified(Email email);
    Mono<User> updateAccountStatus(Email email, String status);
    Mono<Void> changePassword(Email email, String newPassword);
    Mono<Void> deleteUser(Email email);
    Mono<UserStatistics> getUserStatistics(UserId userId);
    Mono<Page<User>> searchUsers(String searchTerm, int page, int size);
}
