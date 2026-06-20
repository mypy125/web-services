package com.mygitgor.user_service.infrastructure.client.fallback;

import com.mygitgor.user_service.infrastructure.dto.external.PaymentMethodDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentServiceFallback {

    public Mono<PaymentMethodDto> getDefaultPaymentMethod(String userId) {
        log.warn("Fallback: Returning empty default payment method for user: {}", userId);
        return Mono.empty();
    }

    public Flux<PaymentMethodDto> getUserPaymentMethods(String userId) {
        log.warn("Fallback: Returning empty payment methods list for user: {}", userId);
        return Flux.empty();
    }

    public Mono<PaymentMethodDto> addPaymentMethod(String userId, PaymentMethodDto paymentMethod) {
        log.warn("Fallback: Could not add payment method for user: {}", userId);
        return Mono.empty();
    }

    public Mono<PaymentMethodDto> updateDefaultPaymentMethod(String userId, String paymentMethodId) {
        log.warn("Fallback: Could not update default payment method for user: {}", userId);
        return Mono.empty();
    }

    public Mono<Void> deletePaymentMethod(String userId, String paymentMethodId) {
        log.warn("Fallback: Could not delete payment method for user: {}, method: {}", userId, paymentMethodId);
        return Mono.empty();
    }

    public Mono<Boolean> validatePaymentMethod(String userId, String paymentMethodId) {
        log.warn("Fallback: Payment method validation failed for user: {}, method: {}", userId, paymentMethodId);
        return Mono.just(false);
    }
}
