package com.mygitgor.user_service.domain.service;

import com.mygitgor.user_service.domain.repository.UserRepository;
import com.mygitgor.user_service.domain.specification.UserEmailUniquenessSpec;
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
    private final UserRepository userRepository;
    private final UserEmailUniquenessSpec emailUniquenessSpec;

    public Mono<Void> validateEmailUniqueness(Email email) {
        return emailUniquenessSpec.isSatisfiedBy(email)
                .flatMap(isSatisfied -> {
                    if (!isSatisfied) {
                        return Mono.error(new DomainException("Email already exists: " + email));
                    }
                    return Mono.empty();
                });
    }
}
