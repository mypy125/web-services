package com.mygitgor.auth_service.domain.auth.model.port;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import reactor.core.publisher.Mono;

public interface JwtPort {
    Mono<String> generateToken(String email, String userId, UserRole role);
    long getTokenExpirationSeconds();
    Mono<Boolean> validateToken(String token);
    Mono<String> extractEmail(String token);
    Mono<UserRole> extractRole(String token);
    Mono<String> extractUserId(String token);
}
