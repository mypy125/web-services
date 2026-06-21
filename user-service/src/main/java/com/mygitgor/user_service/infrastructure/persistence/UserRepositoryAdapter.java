package com.mygitgor.user_service.infrastructure.persistence;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.domain.repository.UserRepositoryPort;
import com.mygitgor.user_service.infrastructure.persistence.entity.UserEntity;
import com.mygitgor.user_service.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.mygitgor.user_service.infrastructure.persistence.repository.UserR2dbcRepository;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Page;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserR2dbcRepository repository;
    private final UserPersistenceMapper mapper;

    @Override
    public Mono<User> save(User user) {
        log.debug("Saving user: {}", user.getId());
        UserEntity entity = mapper.toEntity(user);
        return repository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(saved -> log.debug("User saved successfully: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to save user: {}", error.getMessage()));
    }

    @Override
    public Mono<User> findById(UserId id) {
        log.debug("Finding user by ID: {}", id);
        return repository.findById(UUID.fromString(id.toString()))
                .map(mapper::toDomain)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("User found: {}", id);
                    } else {
                        log.debug("User not found: {}", id);
                    }
                })
                .doOnError(error -> log.error("Failed to find user by ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<User> findByEmail(Email email) {
        log.debug("Finding user by email: {}", email);
        return repository.findByEmail(email.toString())
                .map(mapper::toDomain)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("User found by email: {}", email);
                    } else {
                        log.debug("User not found by email: {}", email);
                    }
                })
                .doOnError(error -> log.error("Failed to find user by email {}: {}", email, error.getMessage()));
    }

    @Override
    public Flux<User> findAll(int page, int size) {
        log.debug("Finding all users - page: {}, size: {}", page, size);
        return repository.findAllBy(PageRequest.of(page, size))
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("All users fetched"))
                .doOnError(error -> log.error("Failed to find all users: {}", error.getMessage()));

    }

    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        log.debug("Checking existence by email: {}", email);
        return repository.existsByEmail(email.toString())
                .doOnSuccess(exists -> log.debug("Email {} exists: {}", email, exists))
                .doOnError(error -> log.error("Failed to check existence by email {}: {}", email, error.getMessage()));
    }

    @Override
    public Mono<Void> deleteByEmail(Email email) {
        log.debug("Deleting user by email: {}", email);
        return repository.deleteByEmail(email.toString())
                .doOnSuccess(v -> log.debug("User deleted by email: {}", email))
                .doOnError(error -> log.error("Failed to delete user by email {}: {}", email, error.getMessage()));
    }

    @Override
    public Mono<Long> count() {
        log.debug("Counting total users");
        return repository.count()
                .doOnSuccess(count -> log.debug("Total users count: {}", count))
                .doOnError(error -> log.error("Failed to count users: {}", error.getMessage()));
    }

    @Override
    public Mono<UserStatistics> getStatistics(UserId userId) {
        log.debug("Getting statistics for user: {}", userId);
        return repository.findById(UUID.fromString(userId.toString()))
                .map(entity -> {
                    return UserStatistics.builder()
                            .userId(userId)
                            .totalOrders(entity.totalOrdersCount() != null ? entity.totalOrdersCount() : 0)
                            .totalSpent(entity.totalSpentAmount() != null ? entity.totalSpentAmount() : 0.0)
                            .build();
                })
                .doOnSuccess(stats -> log.debug("Statistics retrieved for user: {}", userId))
                .doOnError(error -> log.error("Failed to get statistics for user {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<Page<User>> search(String searchTerm, int page, int size) {
        log.debug("Searching users with term: {}, page: {}, size: {}", searchTerm, page, size);

        int offset = page * size;
        return repository.searchByEmailOrFullNameWithPagination(searchTerm, size, offset)
                .map(mapper::toDomain)
                .collectList()
                .zipWith(repository.searchByEmailOrFullName(searchTerm).count())
                .map(tuple -> {
                    List<User> users = tuple.getT1();
                    Long total = tuple.getT2();
                    return Page.of(users, page, size, total);
                })
                .doOnSuccess(result -> log.debug("Search completed, found {} users", result.getTotalElements()))
                .doOnError(error -> log.error("Failed to search users: {}", error.getMessage()));
    }

    @Override
    public Flux<User> findByIds(List<UserId> userIds) {
        log.debug("Finding users by IDs: {}", userIds);
        List<UUID> uuids = userIds.stream()
                .map(id -> UUID.fromString(id.toString()))
                .collect(Collectors.toList());
        return repository.findByIdIn(uuids)
                .map(mapper::toDomain)
                .doOnComplete(() -> log.debug("Users found by IDs"))
                .doOnError(error -> log.error("Failed to find users by IDs: {}", error.getMessage()));
    }

    @Override
    public Mono<Long> countByStatus(AccountStatus status) {
        log.debug("Counting users by status: {}", status);
        return repository.countByAccountStatus(status.name())
                .doOnSuccess(count -> log.debug("Users with status {}: {}", status, count))
                .doOnError(error -> log.error("Failed to count users by status: {}", error.getMessage()));
    }

    @Override
    public Mono<Long> countByRole(UserRole role) {
        log.debug("Counting users by role: {}", role);
        return repository.countByRole(role.name())
                .doOnSuccess(count -> log.debug("Users with role {}: {}", role, count))
                .doOnError(error -> log.error("Failed to count users by role: {}", error.getMessage()));
    }
}
