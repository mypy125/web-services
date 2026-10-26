package com.mygitgor.seller_service.infrastructure.persistence;

import com.mygitgor.seller_service.domain.model.type.AddressType;
import com.mygitgor.seller_service.domain.repository.AddressRepositoryPort;
import com.mygitgor.seller_service.infrastructure.persistence.repository.AddressR2dbcRepository;
import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AddressRepositoryAdapter implements AddressRepositoryPort {
    private final AddressR2dbcRepository addressR2dbcRepository;

    @Override
    public Mono<Address> save(Address address) {
        return null;
    }

    @Override
    public Mono<Address> saveForSeller(SellerId sellerId, Address address) {
        return null;
    }

    @Override
    public Mono<Address> update(Address address) {
        return null;
    }

    @Override
    public Mono<Void> setAsDefault(SellerId sellerId, String addressId, AddressType addressType) {
        return null;
    }

    @Override
    public Mono<Void> deleteById(String addressId) {
        return null;
    }

    @Override
    public Mono<Void> deleteBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Void> deleteBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        return null;
    }

    @Override
    public Mono<Address> findById(String addressId) {
        return null;
    }

    @Override
    public Flux<Address> findBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Flux<Address> findBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        return null;
    }

    @Override
    public Mono<Address> findPickupAddressBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Address> findReturnAddressBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Flux<Address> findWarehouseAddressesBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Address> findDefaultAddressBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        return null;
    }

    @Override
    public Flux<Address> findBySellerIdAndCity(SellerId sellerId, String city) {
        return null;
    }

    @Override
    public Flux<Address> findBySellerIdAndCountry(SellerId sellerId, String country) {
        return null;
    }

    @Override
    public Flux<Address> findByIds(List<String> addressIds) {
        return null;
    }

    @Override
    public Flux<Address> findBySellerIdAndTypeAndCountry(SellerId sellerId, AddressType addressType, String country) {
        return null;
    }

    @Override
    public Flux<Address> findBySellerIdWithPagination(SellerId sellerId, int page, int size) {
        return null;
    }

    @Override
    public Mono<Boolean> existsById(String addressId) {
        return null;
    }

    @Override
    public Mono<Boolean> existsBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        return null;
    }

    @Override
    public Mono<Boolean> hasPickupAddress(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Boolean> hasReturnAddress(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Boolean> hasWarehouseAddresses(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Long> countBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Long> countBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        return null;
    }

    @Override
    public Mono<Long> countWarehouseAddressesBySellerId(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Long> countBySellerIdAndCountry(SellerId sellerId, String country) {
        return null;
    }

    @Override
    public Flux<Address> saveAll(SellerId sellerId, List<Address> addresses) {
        return null;
    }

    @Override
    public Flux<Address> updateAll(List<Address> addresses) {
        return null;
    }

    @Override
    public Mono<Void> deleteAllByIds(List<String> addressIds) {
        return null;
    }

    @Override
    public Mono<Void> deleteAllBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        return null;
    }

    @Override
    public Flux<Address> findBySellerIdAndLocationWithin(SellerId sellerId, Double latitude, Double longitude, Double radiusInKm) {
        return null;
    }

    @Override
    public Flux<Address> findBySellerIdAndCityAndLocationWithin(SellerId sellerId, String city, Double latitude, Double longitude, Double radiusInKm) {
        return null;
    }

    @Override
    public Mono<Boolean> isValidAddress(Address address) {
        return null;
    }

    @Override
    public Mono<Boolean> isDefaultAddress(SellerId sellerId, String addressId, AddressType addressType) {
        return null;
    }
}
