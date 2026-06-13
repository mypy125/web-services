package com.mygitgor.user_service.domain.port.incoming;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateProfileRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateUserRequest;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

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
}
