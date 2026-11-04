package com.mygitgor.seller_service.infrastructure.persistence.repository;

import com.mygitgor.seller_service.infrastructure.persistence.entity.AddressEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressR2dbcRepository extends ReactiveCrudRepository<AddressEntity, UUID> {
    Flux<AddressEntity> findBySellerId(UUID sellerId);
    Flux<AddressEntity> findBySellerId(UUID sellerId, Pageable pageable);
    Flux<AddressEntity> findBySellerIdAndAddressType(UUID sellerId, String addressType);
    Mono<AddressEntity> findBySellerIdAndAddressTypeAndIsDefaultTrue(UUID sellerId, String addressType);
    Flux<AddressEntity> findBySellerIdAndCity(UUID sellerId, String city);
    Flux<AddressEntity> findBySellerIdAndCountry(UUID sellerId, String country);
    Flux<AddressEntity> findBySellerIdAndAddressTypeAndCountry(UUID sellerId, String addressType, String country);
    Flux<AddressEntity> findByIdIn(List<UUID> ids);

    Mono<Void> deleteBySellerId(UUID sellerId);
    Mono<Void> deleteBySellerIdAndAddressType(UUID sellerId, String addressType);

    Mono<Boolean> existsBySellerIdAndAddressType(UUID sellerId, String addressType);

    Mono<Long> countBySellerId(UUID sellerId);
    Mono<Long> countBySellerIdAndAddressType(UUID sellerId, String addressType);
    Mono<Long> countBySellerIdAndCountry(UUID sellerId, String country);
}
