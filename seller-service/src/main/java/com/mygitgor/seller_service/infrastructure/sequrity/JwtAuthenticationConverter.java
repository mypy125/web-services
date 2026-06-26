package com.mygitgor.seller_service.infrastructure.sequrity;

import com.mygitgor.seller_service.infrastructure.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found for seller-service request");
            return Mono.empty();
        }

        String token = authHeader.substring(7);
        log.debug("Validating token for seller-service...");

        return authServiceClient.validateToken(token)
                .flatMap(isValid -> {
                    if (!isValid) {
                        log.warn("Token validation failed in seller-service");
                        return Mono.empty();
                    }
                    return authServiceClient.getUserInfoFromToken(token);
                })
                .map(userInfo -> {
                    String formattedRole = userInfo.role().startsWith("ROLE_") ? userInfo.role() : "ROLE_" + userInfo.role();

                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(formattedRole)
                    );

                    AuthUser authUser = new AuthUser(
                            userInfo.email(),
                            userInfo.id(),
                            userInfo.role()
                    );

                    log.debug("Authenticated seller/admin: {}, role: {}", userInfo.email(), userInfo.role());

                    return new UsernamePasswordAuthenticationToken(authUser, token, authorities);
                });
    }
}
