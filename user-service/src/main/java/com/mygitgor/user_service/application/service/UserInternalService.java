package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.application.mapper.UserMapper;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserId;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.domain.repository.UserRepository;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.dto.request.UserAuthInfoDto;
import com.mygitgor.user_service.infrastructure.dto.request.CreateUserRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInternalService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Mono<Boolean> existsByEmail(Email email) {
        return userRepository.existsByEmail(email);
    }

    public Mono<UserAuthInfoDto> getAuthInfo(Email email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toAuthInfoDto);
    }

    public Mono<UserDto> getUserById(UserId userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto);
    }

    public Mono<UserDto> createUser(CreateUserRequest request) {
        User user = User.register(
                new Email(request.getEmail()),
                request.getFullName(),
                request.getRole() != null ? request.getRole() : UserRole.ROLE_CUSTOMER
        );

        return userRepository.save(user)
                .map(userMapper::toDto);
    }

}
