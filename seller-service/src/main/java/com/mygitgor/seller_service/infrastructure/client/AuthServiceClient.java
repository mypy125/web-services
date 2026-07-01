package com.mygitgor.seller_service.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.seller_service.application.dto.response.ApiResponse;
import com.mygitgor.seller_service.application.dto.response.UserAuthInfoResponse;
import com.mygitgor.seller_service.domain.port.outgoing.AuthPort;
import com.mygitgor.seller_service.shared.valueobject.Email;
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
public class AuthServiceClient implements AuthPort {
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${auth.service.url:http://localhost:8081/api/v1/auth}")
    private String authServiceUrl;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    @Override
    public Mono<Boolean> verifyOtp(Email email, String otp) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/seller/verify")
                        .queryParam("email", email)
                        .queryParam("otp", otp)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .map(responseEntity -> responseEntity.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return webClient.get()
                .uri("/validate")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .map(ApiResponse::isSuccess)
                .onErrorReturn(false);
    }

    @Override
    public Mono<UserAuthInfoResponse> getUserInfoFromToken(String token) {
        return webClient.get()
                .uri("/user-info")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .map(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        throw new IllegalStateException("Failed to get valid user info from auth-service");
                    }
                    return objectMapper.convertValue(response.getData(), UserAuthInfoResponse.class);
                });
    }
}

