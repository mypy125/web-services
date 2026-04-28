package com.mygitgor.auth_service.infrastrucrure.sequrity;

import com.mygitgor.auth_service.infrastrucrure.cache.TokenCacheService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final TokenCacheService tokenCacheService;
    private final JwtProvider jwtProvider;
    private final JwtProps jwtProps;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter jwtWebFilter = new AuthenticationWebFilter(reactiveAuthenticationManager());
        jwtWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter());
        jwtWebFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/otp").permitAll()
                        .pathMatchers("/auth/login/**").permitAll()
                        .pathMatchers("/auth/register/**").permitAll()
                        .pathMatchers("/auth/seller/register").permitAll()
                        .pathMatchers("/auth/seller/verify").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            log.error("Authentication error: {}", ex.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            log.error("Access denied: {}", ex.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                )
                .addFilterAt(jwtWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return authentication -> {
            return Mono.just(authentication);
        };
    }

    @Bean
    public ServerAuthenticationConverter jwtAuthenticationConverter() {
        return exchange -> {
            try {
                String authHeader = exchange.getRequest().getHeaders().getFirst(jwtProps.getHeader());
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.debug("No valid Authorization header found");
                    return Mono.empty();
                }

                String token = authHeader.substring(7);
                log.debug("Validating token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

                if (tokenCacheService.isTokenBlacklisted(token)) {
                    log.warn("Token is blacklisted");
                    return Mono.error(new RuntimeException("Token is blacklisted"));
                }

                if (!jwtProvider.validateToken(token)) {
                    log.warn("Invalid token");
                    return Mono.error(new RuntimeException("Invalid token"));
                }

                String email = jwtProvider.getEmailFromJwtToken(token);
                String userId = jwtProvider.getUserIdFromJwtToken(token);
                String role = jwtProvider.getRoleFromJwtToken(token).name();
                List<String> authorities = jwtProvider.getAuthorities(token);

                log.debug("Authenticated user: {}, role: {}", email, role);

                List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        new AuthUser(email, userId, role),
                        token,
                        grantedAuthorities
                );

                return Mono.just(auth)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("Token expired: {}", e.getMessage());
                return Mono.error(new RuntimeException("Token expired"));
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                log.warn("Malformed token: {}", e.getMessage());
                return Mono.error(new RuntimeException("Invalid token format"));
            } catch (Exception e) {
                log.error("Authentication error: {}", e.getMessage());
                return Mono.error(new RuntimeException("Authentication failed: " + e.getMessage()));
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "https://ecommerce-multivendor-frontend.onrender.com",
                "https://ecommerce-multivendor-frontend-ijkm.onrender.com"
        ));
        configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Correlation-Id"
        ));
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "X-Correlation-Id"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
