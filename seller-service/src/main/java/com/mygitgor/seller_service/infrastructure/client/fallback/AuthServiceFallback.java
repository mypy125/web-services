package com.mygitgor.seller_service.infrastructure.client.fallback;

import com.mygitgor.seller_service.application.dto.response.UserAuthInfoResponse;
import com.mygitgor.seller_service.shared.valueobject.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthServiceFallback {
    public Mono<Boolean> verifyOtpFallback(Email email, String otp, Throwable t) {
        log.warn("Fallback: verifyOtp failed for email: {} due to: {}. Returning safe default: false", email, t.getMessage());
        return Mono.just(false);
    }

    public Mono<Boolean> validateTokenFallback(String token, Throwable t) {
        log.warn("Fallback: validateToken failed due to: {}. Returning safe default: false", t.getMessage());
        return Mono.just(false);
    }

    public Mono<UserAuthInfoResponse> getUserInfoFromTokenFallback(String token, Throwable t) {
        log.error("Fallback: getUserInfoFromToken failed due to: {}. Cannot recover user data.", t.getMessage());
        return Mono.error(t);
    }
}
