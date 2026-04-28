package com.mygitgor.auth_service.infrastrucrure.sequrity;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

public class SecurityUtils {

    public static Mono<AuthUser> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication != null && authentication.isAuthenticated())
                .map(authentication -> (AuthUser) authentication.getPrincipal())
                .switchIfEmpty(Mono.error(new RuntimeException("User not authenticated")));
    }

    public static Mono<String> getCurrentUserId() {
        return getCurrentUser().map(AuthUser::getUserId);
    }

    public static Mono<String> getCurrentUserEmail() {
        return getCurrentUser().map(AuthUser::getEmail);
    }

    public static Mono<String> getCurrentUserRole() {
        return getCurrentUser().map(AuthUser::getRole);
    }
}
