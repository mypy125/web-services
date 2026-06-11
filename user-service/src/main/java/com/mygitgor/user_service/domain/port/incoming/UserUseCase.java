package com.mygitgor.user_service.domain.port.incoming;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import reactor.core.publisher.Mono;

public interface UserUseCase {
    Mono<User> createUser(Email email, String fullName, String phoneNumber, UserRole role);
    Mono<User> updateUser(UserId userId, String fullName, String phoneNumber, String profileImage);
    Mono<User> verifyEmail(Email email);
    Mono<Void> deleteUser(Email email);
    Mono<User> changePassword(Email email, String newPassword);
    Mono<User> updateAccountStatus(Email email, AccountStatus status, String reason);
}
