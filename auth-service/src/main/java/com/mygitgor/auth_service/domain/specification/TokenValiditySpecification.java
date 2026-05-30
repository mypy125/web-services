package com.mygitgor.auth_service.domain.specification;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.enums.TokenStatus;
import com.mygitgor.auth_service.domain.auth.repository.BlacklistedTokenRepository;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenValiditySpecification {
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public Mono<Boolean> isSatisfiedBy(Token token) {
        if (token == null || !token.isValid()) {
            return Mono.just(false);
        }
        return isBlacklisted(token)
                .map(isBlacklisted -> !isBlacklisted);
    }

    public Mono<Void> check(Token token) {
        if (token == null) {
            return Mono.error(new DomainException("Token is null"));
        }
        if (token.isExpired()) {
            return Mono.error(new DomainException("Token has expired"));
        }
        if (token.getStatus() != TokenStatus.ACTIVE) {
            return Mono.error(new DomainException("Token is not active"));
        }

        return isBlacklisted(token)
                .flatMap(blacklisted -> {
                    if (blacklisted) {
                        return Mono.error(new DomainException("Token is blacklisted"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Boolean> isBlacklisted(Token token) {
        return blacklistedTokenRepository.existsByToken(token.getValue().toString())
                .doOnNext(exists -> {
                    if (exists) {
                        log.debug("Token is blacklisted: {}", token.getValue());
                    }
                });
    }
}
