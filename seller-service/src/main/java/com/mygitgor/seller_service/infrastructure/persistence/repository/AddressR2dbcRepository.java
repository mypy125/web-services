package com.mygitgor.seller_service.infrastructure.persistence.repository;

import com.mygitgor.seller_service.infrastructure.persistence.entity.AddressEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface AddressR2dbcRepository extends ReactiveCrudRepository<AddressEntity, UUID> {
    Flux<AddressEntity> findAllBySellerId(UUID sellerId);
    Flux<AddressEntity> findAllBySellerIdAndAddressType(UUID sellerId, String addressType);
    Mono<Void> deleteBySellerIdAndId(UUID sellerId, UUID id);
}
