package com.mygitgor.user_service.domain.repository;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRepositoryPort {
    Mono<User> save(User user);
    Mono<User> findById(UserId id);
    Mono<User> findByEmail(Email email);
    Flux<User> findAll(int page, int size);
    Mono<Boolean> existsByEmail(Email email);
    Mono<Void> deleteByEmail(Email email);
    Mono<Long> count();
    Mono<UserStatistics> getStatistics(UserId userId);
    Mono<Page<User>> search(String searchTerm, int page, int size);
    Flux<User> findByIds(List<UserId> userIds);
    Mono<Long> countByStatus(AccountStatus status);
    Mono<Long> countByRole(UserRole role);
}
