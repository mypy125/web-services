package com.mygitgor.user_service.infrastructure.client.fallback;

import com.mygitgor.user_service.application.dto.external.AddressDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AddressServiceFallback {

    public Mono<AddressDto> getDefaultAddress(String userId) {
        log.warn("Fallback: Returning empty address for user: {}", userId);
        return Mono.empty();
    }

    public Mono<Void> updateDefaultAddress(String userId, String addressId) {
        log.warn("Fallback: Could not update default address for user: {} to {}", userId, addressId);
        return Mono.empty();
    }
}
