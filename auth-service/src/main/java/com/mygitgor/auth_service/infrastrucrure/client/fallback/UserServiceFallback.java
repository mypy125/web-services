package com.mygitgor.auth_service.infrastrucrure.client.fallback;

import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.user.model.User;
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
}
