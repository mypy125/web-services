package com.mygitgor.user_service.domain.specification;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserAccountStatusSpec {

    public Mono<Boolean> isSatisfiedBy(User user) {
        if (user == null) {
            return Mono.just(false);
        }
        boolean isActive = user.getAccountStatus() == AccountStatus.ACTIVE;
        log.debug("User {} account status is active: {}", user.getEmail(), isActive);
        return Mono.just(isActive);
    }

    public Mono<Boolean> isNotBanned(User user) {
        if (user == null) {
            return Mono.just(false);
        }
        boolean isNotBanned = user.getAccountStatus() != AccountStatus.BANNED;
        log.debug("User {} is not banned: {}", user.getEmail(), isNotBanned);
        return Mono.just(isNotBanned);
    }

    public Mono<Boolean> isNotSuspended(User user) {
        if (user == null) {
            return Mono.just(false);
        }
        boolean isNotSuspended = user.getAccountStatus() != AccountStatus.SUSPENDED;
        log.debug("User {} is not suspended: {}", user.getEmail(), isNotSuspended);
        return Mono.just(isNotSuspended);
    }

    public Mono<Boolean> canPerformOperations(User user) {
        return isSatisfiedBy(user)
                .flatMap(isActive -> isNotBanned(user)
                        .map(isNotBanned -> isActive && isNotBanned));
    }
}
