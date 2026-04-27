package com.mygitgor.auth_service.infrastrucrure.persistance.repository;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.repository.TokenRepository;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {

    private final TokenJpaRepository jpaRepository;
    private final TokenMapper mapper;

    @Override
    public Mono<Token> save(Token token) {
        return Mono.fromCallable(() -> {
            TokenEntity entity = mapper.toEntity(token);
            TokenEntity saved = jpaRepository.save(entity);
            log.debug("Token saved for user: {}", token.getEmail());
            return mapper.toDomain(saved);
        });
    }

    @Override
    public Mono<Token> findByValue(TokenValue value) {
        return Mono.fromCallable(() ->
                jpaRepository.findByValue(value.toString())
                        .map(mapper::toDomain)
                        .orElse(null)
        );
    }

    @Override
    public Mono<Token> findActiveTokenByUserId(UserId userId) {
        return Mono.fromCallable(() ->
                jpaRepository.findActiveTokenByUserId(UUID.fromString(userId.toString()))
                        .map(mapper::toDomain)
                        .orElse(null)
        );
    }

    @Override
    public Flux<Token> findAllByEmail(Email email) {
        return Flux.fromIterable(
                jpaRepository.findAllByEmail(email.toString())
                        .stream()
                        .map(mapper::toDomain)
                        .toList()
        );
    }

    @Override
    public Flux<Token> findAllByUserId(UserId userId) {
        return Flux.fromIterable(
                jpaRepository.findAllByUserId(UUID.fromString(userId.toString()))
                        .stream()
                        .map(mapper::toDomain)
                        .toList()
        );
    }

    @Override
    public Mono<Void> delete(Token token) {
        return Mono.fromRunnable(() -> {
            jpaRepository.deleteByValue(token.getValue().toString());
            log.debug("Token deleted for user: {}", token.getEmail());
        });
    }

    @Override
    public Mono<Void> deleteAllByEmail(Email email) {
        return Mono.fromRunnable(() -> {
            jpaRepository.deleteAllByEmail(email.toString());
            log.debug("All tokens deleted for user: {}", email);
        });
    }

    @Override
    public Mono<Void> deleteAllByUserId(UserId userId) {
        return Mono.fromRunnable(() -> {
            jpaRepository.deleteAllByUserId(UUID.fromString(userId.toString()));
            log.debug("All tokens deleted for user ID: {}", userId);
        });
    }

    @Override
    public Mono<Boolean> existsByValue(TokenValue value) {
        return Mono.fromCallable(() -> jpaRepository.existsByValue(value.toString()));
    }
}
