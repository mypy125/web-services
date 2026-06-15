package com.mygitgor.user_service.domain.specification;


import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserEmailVerifiedSpec {

    public Mono<Boolean> isSatisfiedBy(User user) {
        if (user == null) {
            return Mono.just(false);
        }
        boolean isVerified = user.isEmailVerified();
        log.debug("User {} email verified: {}", user.getEmail(), isVerified);
        return Mono.just(isVerified);
    }

    public Mono<Boolean> isNotVerified(User user) {
        return isSatisfiedBy(user)
                .map(isVerified -> !isVerified);
    }

    public Mono<Boolean> isVerifiedForSeller(User user, UserRole role) {
        if (role != UserRole.ROLE_SELLER) {
            return Mono.just(true);
        }
        return isSatisfiedBy(user);
    }
}
