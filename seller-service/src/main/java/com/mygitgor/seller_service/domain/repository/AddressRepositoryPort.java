package com.mygitgor.seller_service.domain.repository;

import com.mygitgor.seller_service.domain.model.shared.valueobject.Address;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.model.shared.valueobject.type.AddressType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AddressRepositoryPort {
    Mono<Address> save(Address address);
    Mono<Address> saveForSeller(SellerId sellerId, Address address);
    Mono<Address> update(Address address);
    Mono<Void> setAsDefault(SellerId sellerId, String addressId, AddressType addressType);
    Mono<Void> deleteById(String addressId);
    Mono<Void> deleteBySellerId(SellerId sellerId);
    Mono<Void> deleteBySellerIdAndType(SellerId sellerId, AddressType addressType);
    Mono<Address> findById(String addressId);
    Flux<Address> findBySellerId(SellerId sellerId);
    Flux<Address> findBySellerIdAndType(SellerId sellerId, AddressType addressType);
    Mono<Address> findPickupAddressBySellerId(SellerId sellerId);
    Mono<Address> findReturnAddressBySellerId(SellerId sellerId);
    Flux<Address> findWarehouseAddressesBySellerId(SellerId sellerId);
    Mono<Address> findDefaultAddressBySellerIdAndType(SellerId sellerId, AddressType addressType);
    Flux<Address> findBySellerIdAndCity(SellerId sellerId, String city);
    Flux<Address> findBySellerIdAndCountry(SellerId sellerId, String country);
    Flux<Address> findByIds(List<String> addressIds);
    Flux<Address> findBySellerIdAndTypeAndCountry(
            SellerId sellerId,
            AddressType addressType,
            String country
    );
    Flux<Address> findBySellerIdWithPagination(SellerId sellerId, int page, int size);
    Mono<Boolean> existsById(String addressId);
    Mono<Boolean> existsBySellerIdAndType(SellerId sellerId, AddressType addressType);
    Mono<Boolean> hasPickupAddress(SellerId sellerId);
    Mono<Boolean> hasReturnAddress(SellerId sellerId);
    Mono<Boolean> hasWarehouseAddresses(SellerId sellerId);
    Mono<Long> countBySellerId(SellerId sellerId);
    Mono<Long> countBySellerIdAndType(SellerId sellerId, AddressType addressType);
    Mono<Long> countWarehouseAddressesBySellerId(SellerId sellerId);
    Mono<Long> countBySellerIdAndCountry(SellerId sellerId, String country);
    Flux<Address> saveAll(SellerId sellerId, List<Address> addresses);
    Flux<Address> updateAll(List<Address> addresses);
    Mono<Void> deleteAllByIds(List<String> addressIds);
    Mono<Void> deleteAllBySellerIdAndType(SellerId sellerId, AddressType addressType);
    Flux<Address> findBySellerIdAndLocationWithin(
            SellerId sellerId,
            Double latitude,
            Double longitude,
            Double radiusInKm
    );
    Flux<Address> findBySellerIdAndCityAndLocationWithin(
            SellerId sellerId,
            String city,
            Double latitude,
            Double longitude,
            Double radiusInKm
    );
    Mono<Boolean> isValidAddress(Address address);
    Mono<Boolean> isDefaultAddress(SellerId sellerId, String addressId, AddressType addressType);
}
