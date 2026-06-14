package com.mygitgor.user_service.infrastructure.persistence;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.domain.repository.UserRepositoryPort;
import com.mygitgor.user_service.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.mygitgor.user_service.infrastructure.persistence.repository.UserR2dbcRepository;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserR2dbcRepository repository;
    private final UserPersistenceMapper mapper;

    @Override
    public Mono<User> save(User user) {
        return null;
    }

    @Override
    public Mono<User> findById(UserId id) {
        return null;
    }

    @Override
    public Mono<User> findByEmail(Email email) {
        return null;
    }

    @Override
    public Flux<User> findAll(int page, int size) {
        return null;
    }

    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        return null;
    }

    @Override
    public Mono<Void> deleteByEmail(Email email) {
        return null;
    }

    @Override
    public Mono<Long> count() {
        return null;
    }

    @Override
    public Mono<UserStatistics> getStatistics(UserId userId) {
        return null;
    }

    @Override
    public Mono<Page<User>> search(String searchTerm, int page, int size) {
        return null;
    }

    @Override
    public Flux<User> findByIds(List<UserId> userIds) {
        return null;
    }

    @Override
    public Mono<Long> countByStatus(AccountStatus status) {
        return null;
    }

    @Override
    public Mono<Long> countByRole(UserRole role) {
        return null;
    }
}
