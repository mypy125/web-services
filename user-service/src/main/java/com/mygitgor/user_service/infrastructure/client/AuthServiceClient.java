package com.mygitgor.user_service.infrastructure.client;

import com.mygitgor.user_service.application.dto.response.UserAuthInfoResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    public Mono<Boolean> validateToken(String token) {
        return webClient.post()
                .uri("/auth/validate")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }

    public Mono<UserAuthInfoResponse> getUserInfoFromToken(String token) {
        return webClient.get()
                .uri("/auth/user-info")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserAuthInfoResponse.class);
    }
}
