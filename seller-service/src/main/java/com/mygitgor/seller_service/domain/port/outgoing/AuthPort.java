package com.mygitgor.seller_service.domain.port.outgoing;

import com.mygitgor.seller_service.application.dto.response.UserAuthInfoResponse;
import com.mygitgor.seller_service.shared.valueobject.Email;
import reactor.core.publisher.Mono;

public interface AuthPort {
    Mono<Boolean> verifyOtp(Email email, String otp);
    Mono<Boolean> validateToken(String token);
    Mono<UserAuthInfoResponse> getUserInfoFromToken(String token);
}
