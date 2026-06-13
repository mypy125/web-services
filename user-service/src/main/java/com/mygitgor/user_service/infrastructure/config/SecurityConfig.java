package com.mygitgor.user_service.infrastructure.config;

import com.mygitgor.user_service.infrastructure.security.JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    @Order(1)
    public SecurityWebFilterChain internalSecurityWebFilterChain(ServerHttpSecurity http) {
        log.debug("Configuring Internal API Security (for service-to-service communication)");

        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/internal/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(internalCorsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .anyExchange().permitAll()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain publicSecurityWebFilterChain(ServerHttpSecurity http) {
        log.debug("Configuring Public API Security");

        AuthenticationWebFilter jwtWebFilter = new AuthenticationWebFilter(reactiveAuthenticationManager());
        jwtWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter);
        jwtWebFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(publicCorsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/health", "/actuator/info", "/actuator/ready", "/actuator/live").permitAll()
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .pathMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/users/{userId}").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/v1/users/me").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/v1/users/{userId}").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/me").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/{userId}").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/me/verify-email").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/me/change-password").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/me/profile-image").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/me/profile-image").authenticated()

                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            log.error("Authentication error: {}", ex.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            String jsonResponse = String.format(
                                    "{\"error\":\"Unauthorized\", \"message\":\"%s\", \"timestamp\":\"%s\"}",
                                    ex.getMessage(), java.time.LocalDateTime.now()
                            );
                            byte[] response = jsonResponse.getBytes();
                            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(response)));
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            log.error("Access denied: {}", ex.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            String jsonResponse = String.format(
                                    "{\"error\":\"Access Denied\", \"message\":\"%s\", \"timestamp\":\"%s\"}",
                                    ex.getMessage(), java.time.LocalDateTime.now()
                            );
                            byte[] response = jsonResponse.getBytes();
                            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(response)));
                        })
                )
                .addFilterAt(jwtWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return authentication -> {
            if (authentication.isAuthenticated()) {
                log.debug("Authentication successful for: {}", authentication.getName());
                return Mono.just(authentication);
            }
            log.warn("Authentication failed");
            return Mono.error(new RuntimeException("Authentication failed"));
        };
    }

    @Bean
    public CorsConfigurationSource internalCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Correlation-Id"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CorsConfigurationSource publicCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "https://ecommerce-multivendor-frontend.onrender.com",
                "https://ecommerce-multivendor-frontend-ijkm.onrender.com"
        ));
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Correlation-Id"
        ));
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Correlation-Id"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}