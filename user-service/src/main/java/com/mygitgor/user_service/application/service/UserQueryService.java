package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.port.outgoing.UserRepositoryPort;
import com.mygitgor.user_service.infrastructure.dto.request.UserAuthInfoDto;
import com.mygitgor.user_service.infrastructure.dto.request.UserDto;
import com.mygitgor.user_service.infrastructure.mapper.UserDtoMapper;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepositoryPort userRepository;
    private final UserDtoMapper mapper;

    public Mono<UserDto> getUserById(UserId userId) {
        log.debug("Query: Getting user by ID: {}", userId);
        return userRepository.findById(userId)
                .map(mapper::toDto);
    }

    public Mono<UserAuthInfoDto> getAuthInfo(Email email) {
        log.debug("Query: Getting auth info for email: {}", email);
        return userRepository.findByEmail(email)
                .map(mapper::toAuthInfoDto);
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

    public Mono<Page<UserDto>> searchUsers(String searchTerm, int page, int size) {
        log.debug("Query: Searching users with term: {}", searchTerm);
        return userRepository.search(searchTerm, page, size)
                .map(mapper::toDtoPage);
    }

    public Mono<UserStatisticsDto> getUserStatistics(UserId userId) {
        log.debug("Query: Getting statistics for user: {}", userId);
        return userRepository.getStatistics(userId)
                .map(mapper::toStatisticsDto);
    }
}
