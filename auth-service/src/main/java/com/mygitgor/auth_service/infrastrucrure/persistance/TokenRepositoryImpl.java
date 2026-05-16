package com.mygitgor.auth_service.infrastrucrure.persistance;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.repository.TokenRepository;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.mapper.TokenMapper;
import com.mygitgor.auth_service.infrastrucrure.persistance.entity.TokenEntity;
import com.mygitgor.auth_service.infrastrucrure.persistance.repository.TokenR2dbcRepository;
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
    private final TokenR2dbcRepository r2dbcRepository;
    private final TokenMapper tokenMapper;

    @Override
    public Mono<Token> save(Token token) {
        TokenEntity entity = tokenMapper.toEntity(token);
        return r2dbcRepository.save(entity)
                .map(tokenMapper::toDomain);
    }

    @Override
    public Mono<Token> findByValue(TokenValue value) {
        return r2dbcRepository.findByValue(value.toString())
                .map(tokenMapper::toDomain);
    }

    @Override
    public Mono<Token> findActiveTokenByUserId(UserId userId) {
        return r2dbcRepository.findFirstByUserIdAndStatusOrderByIssuedAtDesc(
                        userId.toString(),
                        "ACTIVE"
                )
                .map(tokenMapper::toDomain);
    }

    @Override
    public Flux<Token> findAllByEmail(Email email) {
        return r2dbcRepository.findAllByEmail(email.toString())
                .map(tokenMapper::toDomain);
    }

    @Override
    public Flux<Token> findAllByUserId(UserId userId) {
        return r2dbcRepository.findAllByUserId(userId.toString())
                .map(tokenMapper::toDomain);
    }

    @Override
    public Mono<Void> delete(Token token) {
        return r2dbcRepository.findByValue(token.getValue().toString())
                .flatMap(r2dbcRepository::delete);
    }

    @Override
    public Mono<Void> deleteAllByEmail(Email email) {
        return r2dbcRepository.deleteAllByEmail(email.toString());
    }

    @Override
    public Mono<Token> update(Token token) {
        return r2dbcRepository.findByValue(token.getValue().toString())
                .flatMap(existingEntity -> {
                    TokenEntity entity = tokenMapper.toEntity(token);
                    entity.setId(existingEntity.getId());
                    return r2dbcRepository.save(entity);
                })
                .map(tokenMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteAllByUserId(UserId userId) {
        return r2dbcRepository.deleteAllByUserId(userId.toString())
                .doOnSuccess(v -> log.debug("All tokens deleted for user ID: {}", userId));
    }

    @Override
    public Mono<Boolean> existsByValue(TokenValue value) {
        return r2dbcRepository.existsByValue(value.toString());
    }
}
