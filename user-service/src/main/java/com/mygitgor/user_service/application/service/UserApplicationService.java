package com.mygitgor.user_service.application.service;

import com.mygitgor.user_service.application.mapper.UserMapper;
import com.mygitgor.user_service.domain.model.UserId;
import com.mygitgor.user_service.domain.repository.UserRepository;
import com.mygitgor.user_service.infrastructure.dto.request.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserInternalService userInternalService;

    public Mono<UserDto> getUserById(UserId userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto);
    }

    public Mono<UserDto> updateUser(UserId userId, UpdateUserRequest request) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.updateProfile(
                            request.getFullName(),
                            request.getProfileImage(),
                            request.getPhoneNumber()
                    );
                    return user;
                })
                .flatMap(userRepository::save)
                .map(userMapper::toDto);
    }

}
