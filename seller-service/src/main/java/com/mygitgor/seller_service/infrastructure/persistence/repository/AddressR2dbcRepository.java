package com.mygitgor.seller_service.infrastructure.persistence.repository;

import com.mygitgor.seller_service.infrastructure.persistence.entity.AddressEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface AddressR2dbcRepository extends R2dbcRepository<AddressEntity, UUID> {
    Flux<AddressEntity> findAllBySellerId(UUID sellerId);
    Flux<AddressEntity> findAllBySellerIdAndAddressType(UUID sellerId, String addressType);
    Mono<Void> deleteBySellerIdAndId(UUID sellerId, UUID id);
}
