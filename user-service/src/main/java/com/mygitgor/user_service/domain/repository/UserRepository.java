package com.mygitgor.user_service.domain.repository;

import com.mygitgor.user_service.domain.model.UserId;
import jakarta.validation.constraints.Email;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User user);
    Mono<User> findById(UserId id);
    Mono<User> findByEmail(Email email);
    Flux<User> findAll(int page, int size);
    Mono<Boolean> existsByEmail(Email email);
    Mono<Void> delete(UserId id);
}
