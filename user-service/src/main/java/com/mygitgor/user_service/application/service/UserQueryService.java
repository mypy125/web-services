package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.repository.UserRepositoryPort;
import com.mygitgor.user_service.infrastructure.dto.response.UserAuthInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserResponse;
import com.mygitgor.user_service.infrastructure.mapper.PageMapper;
import com.mygitgor.user_service.infrastructure.mapper.UserMapper;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Page;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;
    private final PageMapper pageMapper;

    public Mono<UserResponse> getUserById(UserId userId) {
        log.debug("Query: Getting user by ID: {}", userId);
        return userRepository.findById(userId)
                .map(userMapper::toResponse);
    }

    public Mono<UserAuthInfoResponse> getAuthInfo(Email email) {
        log.debug("Query: Getting auth info for email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toAuthInfoResponse);
    }

    public Mono<Boolean> existsByEmail(Email email) {
        log.debug("Query: Checking existence for email: {}", email);
        return userRepository.existsByEmail(email);
    }

    public Mono<Boolean> isEmailVerified(Email email) {
        log.debug("Query: Checking email verified for: {}", email);
        return userRepository.findByEmail(email)
                .map(User::isEmailVerified)
                .defaultIfEmpty(false);
    }

    public Mono<Page<UserResponse>> searchUsers(String searchTerm, int page, int size) {
        log.debug("Query: Searching users with term: {}", searchTerm);
        return userRepository.search(searchTerm, page, size)
                .map(userPage -> pageMapper.map(userPage, userMapper::toResponse));
    }
}
