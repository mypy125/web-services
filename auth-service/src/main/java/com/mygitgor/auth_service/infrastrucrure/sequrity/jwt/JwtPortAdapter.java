package com.mygitgor.auth_service.infrastrucrure.sequrity.jwt;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.auth.model.port.JwtPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtPortAdapter implements JwtPort {
    private final JwtProvider jwtProvider;

    @Override
    public Mono<String> generateToken(String email, String userId, UserRole role) {
        return Mono.fromCallable(() -> {
            return jwtProvider.generateToken(email, role, userId);
        });
    }

    @Override
    public long getTokenExpirationSeconds() {
        return jwtProvider.getJwtProps().getExpirationTime() / 1000;
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> jwtProvider.validateToken(token));
    }

    @Override
    public Mono<String> extractEmail(String token) {
        return Mono.fromCallable(() -> jwtProvider.getEmailFromJwtToken(token));
    }

    @Override
    public Mono<UserRole> extractRole(String token) {
        return Mono.fromCallable(() -> jwtProvider.getRoleFromJwtToken(token));
    }

    @Override
    public Mono<String> extractUserId(String token) {
        return Mono.fromCallable(() -> jwtProvider.getUserIdFromJwtToken(token));
    }
}
