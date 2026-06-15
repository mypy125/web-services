package com.mygitgor.user_service.domain.specification;

import com.mygitgor.user_service.domain.repository.UserRepositoryPort;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEmailUniquenessSpec {
    private final UserRepositoryPort userRepository;

    public Mono<Boolean> isSatisfiedBy(Email email) {
        log.debug("Checking email uniqueness: {}", email);
        return userRepository.existsByEmail(email)
                .map(exists -> !exists)
                .doOnSuccess(isUnique -> log.debug("Email {} is unique: {}", email, isUnique));
    }

    public Mono<Boolean> isSatisfiedBy(Email email, UserId existingUserId) {
        log.debug("Checking email uniqueness for update: {}, userId: {}", email, existingUserId);

        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(existingUserId))
                .defaultIfEmpty(true)
                .doOnSuccess(isUnique -> log.debug("Email {} is unique for update: {}", email, isUnique));
    }
}
