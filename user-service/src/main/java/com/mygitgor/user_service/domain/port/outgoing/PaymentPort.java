package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.infrastructure.dto.external.PaymentMethodDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PaymentPort {
    Mono<PaymentMethodDto> getDefaultPaymentMethod(String userId);
    Mono<List<PaymentMethodDto>> getUserPaymentMethods(String userId);
    Mono<PaymentMethodDto> addPaymentMethod(String userId, PaymentMethodDto paymentMethod);
    Mono<PaymentMethodDto> updateDefaultPaymentMethod(String userId, String paymentMethodId);
    Mono<Void> deletePaymentMethod(String userId, String paymentMethodId);
    Mono<Boolean> validatePaymentMethod(String userId, String paymentMethodId);
}
