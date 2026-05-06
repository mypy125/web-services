package com.mygitgor.auth_service.infrastrucrure.client.fallback;

import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.domain.user.model.User;
import com.mygitgor.auth_service.infrastrucrure.client.dto.UserAuthInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;

@Slf4j
@Component
public class UserServiceFallback {

    public Mono<Boolean> existsByEmail(Email email) {
        log.warn("UserService fallback: existsByEmail for {}", email);
        return Mono.just(false);
    }

    public Mono<User> getUserByEmail(Email email) {
        log.warn("UserService fallback: getUserByEmail for {}", email);
        return Mono.error(new ServiceUnavailableException("User service is temporarily unavailable"));
    }

    public Mono<User> createUser(User user) {
        log.warn("UserService fallback: createUser for {}", user.getEmail());
        return Mono.error(new ServiceUnavailableException("Unable to create user. Service unavailable"));
    }

    public Mono<User> verifyEmail(Email email) {
        log.warn("UserService fallback: verifyEmail for {}", email);
        return Mono.error(new ServiceUnavailableException("Unable to verify email. Service unavailable"));
    }

    public Mono<UserAuthInfoDto> getAuthInfo(Email email) {
        log.warn("UserService fallback: authInfo for {}", email);
        return Mono.error(new ServiceUnavailableException("Unable to auth info email. Service unavailable"));
    }

    public Mono<User> getUserById(UserId userId) {
        log.warn("UserService fallback: getUserById for {}", userId);
        return Mono.error(new ServiceUnavailableException("Unable to get user by id. Service unavailable"));
    }

    public Mono<User> updateUser(User user) {
        log.warn("UserService fallback: update user for {}", user);
        return Mono.error(new ServiceUnavailableException("Unable to update user. Service unavailable"));
    }
}
