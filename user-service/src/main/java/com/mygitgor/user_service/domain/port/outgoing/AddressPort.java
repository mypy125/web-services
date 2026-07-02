package com.mygitgor.user_service.domain.port.outgoing;

import com.mygitgor.user_service.application.dto.external.AddressDto;
import reactor.core.publisher.Mono;

public interface AddressPort {
    Mono<AddressDto> getDefaultAddress(String userId);
    Mono<Void> updateDefaultAddress(String userId, String addressId);
}
