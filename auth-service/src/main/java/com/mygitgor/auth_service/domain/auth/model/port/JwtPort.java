package com.mygitgor.auth_service.domain.auth.model.port;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import reactor.core.publisher.Mono;

public interface JwtPort {
    Mono<String> generateToken(String email, String userId, UserRole role);
    long getTokenExpirationSeconds();
}
