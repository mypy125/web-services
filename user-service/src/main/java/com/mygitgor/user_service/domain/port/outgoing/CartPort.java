package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.infrastructure.dto.external.CartSummaryDto;
import reactor.core.publisher.Mono;

public interface CartPort {
    Mono<CartSummaryDto> getUserCartSummary(String userId);
    Mono<Integer> getCartItemsCount(String userId);
    Mono<Void> clearCart(String userId);
}
