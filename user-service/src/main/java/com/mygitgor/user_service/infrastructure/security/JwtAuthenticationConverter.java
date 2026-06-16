package com.mygitgor.user_service.infrastructure.security;

import com.mygitgor.user_service.infrastructure.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {
    private final AuthServiceClient authServiceClient;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found");
            return Mono.empty();
        }

        String token = authHeader.substring(7);
        log.debug("Validating token: {}...", token.substring(0, Math.min(token.length(), 20)));

        return authServiceClient.validateToken(token)
                .flatMap(isValid -> {
                    if (!isValid) {
                        log.warn("Invalid token");
                        return Mono.empty();
                    }
                    return authServiceClient.getUserInfoFromToken(token);
                })
                .map(userInfo -> {
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + userInfo.role())
                    );

                    AuthUser authUser = new AuthUser(
                            userInfo.email(),
                            userInfo.id(),
                            userInfo.role()
                    );

                    log.debug("Authenticated user: {}, role: {}", userInfo.email(), userInfo.role());

                    return new UsernamePasswordAuthenticationToken(authUser, token, authorities);
                });
    }
}
