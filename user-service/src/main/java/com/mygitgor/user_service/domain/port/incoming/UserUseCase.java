package com.mygitgor.user_service.domain.port.incoming;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateProfileRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateUserRequest;
import com.mygitgor.user_service.shared.valueobject.Email;
import com.mygitgor.user_service.shared.valueobject.Page;
import com.mygitgor.user_service.shared.valueobject.UserAuthInfo;
import com.mygitgor.user_service.shared.valueobject.UserId;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface UserUseCase {
    Mono<User> createUser(Email email, String fullName, String phoneNumber, UserRole role);
    Mono<User> updateUser(UserId userId, UpdateUserRequest req);
    Mono<User> verifyEmail(Email email);
    Mono<Void> deleteUser(Email email);
    Mono<User> changePassword(Email email, String newPassword);
    Mono<User> updateAccountStatus(Email email, AccountStatus status, String reason, String changedBy);
    Mono<User> getUserById(UserId userId);
    Mono<User> updateProfile(UserId userId, UpdateProfileRequest request);
    Mono<User> uploadProfileImage(UserId userId, Mono<FilePart> filePart);
    Mono<User> deleteProfileImage(UserId userId);
    Mono<Void> deleteUserById(UserId userId);
    Mono<User> verifyEmailById(UserId userId);
    Mono<User> changePasswordById(UserId userId, String newPassword);
    Mono<User> activateUser(UserId userId, String activatedBy);
    Mono<User> banUser(UserId userId, String reason, String bannedBy);
    Mono<User> suspendUser(UserId userId, String reason, String suspendedBy);
    Mono<User> updateUserRole(UserId userId, UserRole newRole, String changedBy);
    Mono<Void> updateLastLogin(UserId userId, LocalDateTime lastLoginAt);
    Mono<UserAuthInfo> getUserAuthInfo(Email email);
    Mono<User> updateOrderStatistics(UserId userId, Double orderAmount);
    Mono<User> getUserByEmail(Email email);
    Mono<Boolean> existsByEmail(Email email);
    Mono<Boolean> isEmailVerified(Email email);
    Mono<Page<User>> searchUsers(String searchTerm, int page, int size);
    Flux<User> getUsersByIds(List<UserId> userIds);
    Mono<UserStatistics> getUserStatistics(UserId userId);
    Mono<Long> countUsersByStatus(AccountStatus status);
    Mono<Long> countUsersByRole(UserRole role);
}
