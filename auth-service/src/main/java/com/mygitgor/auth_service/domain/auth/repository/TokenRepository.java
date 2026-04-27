package com.mygitgor.auth_service.domain.auth.repository;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenRepository {
    Mono<Token> save(Token token);
    Mono<Token> findByValue(TokenValue value);
    Mono<Token> findActiveTokenByUserId(UserId userId);
    Flux<Token> findAllByEmail(Email email);
    Flux<Token> findAllByUserId(UserId userId);
    Mono<Void> delete(Token token);
    Mono<Void> deleteAllByEmail(Email email);
    Mono<Void> deleteAllByUserId(UserId userId);
    Mono<Boolean> existsByValue(TokenValue value);
}
