package com.mygitgor.seller_service.infrastructure.persistence;

import com.mygitgor.seller_service.domain.model.type.AddressType;
import com.mygitgor.seller_service.domain.repository.AddressRepositoryPort;
import com.mygitgor.seller_service.infrastructure.persistence.entity.AddressEntity;
import com.mygitgor.seller_service.infrastructure.persistence.mapper.AddressPersistenceMapper;
import com.mygitgor.seller_service.infrastructure.persistence.repository.AddressR2dbcRepository;
import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AddressRepositoryAdapter implements AddressRepositoryPort {
    private final AddressR2dbcRepository repository;
    private final AddressPersistenceMapper mapper;

    @Override
    public Mono<Address> save(Address address) {
        log.debug("Saving address: {}", address.id());
        AddressEntity entity = mapper.toEntity(address);
        return repository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(a -> log.debug("Address saved: {}", a.id()))
                .doOnError(e -> log.error("Failed to save address: {}", e.getMessage()));
    }

    @Override
    public Mono<Address> saveForSeller(SellerId sellerId, Address address) {
        log.debug("Saving address for seller: {}", sellerId);
        LocalDateTime now = LocalDateTime.now();
        AddressEntity entity = mapper.toEntity(address)
                .withSellerId(UUID.fromString(sellerId.toString()))
                .withCreatedAt(now)
                .withUpdatedAt(now);

        return repository.save(entity)
                .map(mapper::toDomain)
                .doOnSuccess(a -> log.debug("Address saved for seller: {}", sellerId));
    }

    @Override
    public Mono<Address> update(Address address) {
        log.debug("Updating address: {}", address.id());
        return repository.findById(UUID.fromString(address.id()))
                .switchIfEmpty(Mono.error(new RuntimeException("Address not found: " + address.id())))
                .flatMap(existing -> {
                    AddressEntity entity = mapper.toEntity(address)
                            .withId(existing.id())
                            .withSellerId(existing.sellerId())
                            .withCreatedAt(existing.createdAt())
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(entity);
                })
                .map(mapper::toDomain)
                .doOnSuccess(a -> log.debug("Address updated: {}", a.id()));
    }

    @Override
    public Mono<Void> setAsDefault(SellerId sellerId, String addressId, AddressType addressType) {
        log.info("Setting default address for seller: {}, type: {}", sellerId, addressType);
        UUID sellerUuid = UUID.fromString(sellerId.toString());
        UUID addressUuid = UUID.fromString(addressId);

        return repository.findBySellerIdAndAddressType(sellerUuid, addressType.name())
                .flatMap(entity -> {
                    if (entity.isDefault()) {
                        AddressEntity updated = entity
                                .withDefault(false)
                                .withUpdatedAt(LocalDateTime.now());
                        return repository.save(updated);
                    }
                    return Mono.just(entity);
                })
                .then(repository.findById(addressUuid))
                .switchIfEmpty(Mono.error(new RuntimeException("Address not found: " + addressId)))
                .flatMap(entity -> {
                    AddressEntity updated = entity
                            .withDefault(true)
                            .withUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .then()
                .doOnSuccess(v -> log.info("Default address set: {}", addressId));
    }

    @Override
    public Mono<Void> deleteById(String addressId) {
        log.debug("Deleting address: {}", addressId);
        return repository.deleteById(UUID.fromString(addressId));
    }

    @Override
    public Mono<Void> deleteBySellerId(SellerId sellerId) {
        log.debug("Deleting all addresses for seller: {}", sellerId);
        return repository.deleteBySellerId(UUID.fromString(sellerId.toString()));
    }

    @Override
    public Mono<Void> deleteBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        log.debug("Deleting addresses for seller: {}, type: {}", sellerId, addressType);
        return repository.deleteBySellerIdAndAddressType(
                UUID.fromString(sellerId.toString()),
                addressType.name());
    }

    @Override
    public Mono<Address> findById(String addressId) {
        log.debug("Finding address by ID: {}", addressId);
        return repository.findById(UUID.fromString(addressId))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> findBySellerId(SellerId sellerId) {
        log.debug("Finding all addresses for seller: {}", sellerId);
        return repository.findBySellerId(UUID.fromString(sellerId.toString()))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> findBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        log.debug("Finding addresses for seller: {}, type: {}", sellerId, addressType);
        return repository.findBySellerIdAndAddressType(
                        UUID.fromString(sellerId.toString()),
                        addressType.name())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Address> findPickupAddressBySellerId(SellerId sellerId) {
        log.debug("Finding pickup address for seller: {}", sellerId);
        return repository.findBySellerIdAndAddressType(
                        UUID.fromString(sellerId.toString()),
                        AddressType.PICKUP.name())
                .next()
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Address> findReturnAddressBySellerId(SellerId sellerId) {
        log.debug("Finding return address for seller: {}", sellerId);
        return repository.findBySellerIdAndAddressType(
                        UUID.fromString(sellerId.toString()),
                        AddressType.RETURN.name())
                .next()
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> findWarehouseAddressesBySellerId(SellerId sellerId) {
        log.debug("Finding warehouse addresses for seller: {}", sellerId);
        return repository.findBySellerIdAndAddressType(
                        UUID.fromString(sellerId.toString()),
                        AddressType.WAREHOUSE.name())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Address> findDefaultAddressBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        log.debug("Finding default address for seller: {}, type: {}", sellerId, addressType);
        return repository.findBySellerIdAndAddressTypeAndIsDefaultTrue(
                        UUID.fromString(sellerId.toString()),
                        addressType.name())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> findBySellerIdAndCity(SellerId sellerId, String city) {
        log.debug("Finding addresses for seller: {}, city: {}", sellerId, city);
        return repository.findBySellerIdAndCity(
                        UUID.fromString(sellerId.toString()),
                        city)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> findBySellerIdAndCountry(SellerId sellerId, String country) {
        log.debug("Finding addresses for seller: {}, country: {}", sellerId, country);
        return repository.findBySellerIdAndCountry(
                        UUID.fromString(sellerId.toString()),
                        country)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> findByIds(List<String> addressIds) {
        List<UUID> uuids = addressIds.stream()
                .map(UUID::fromString)
                .toList();
        return repository.findByIdIn(uuids)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> findBySellerIdAndTypeAndCountry(SellerId sellerId,
                                                         AddressType addressType,
                                                         String country
    ) {
        log.debug("Finding addresses for seller: {}, type: {}, country: {}",
                sellerId, addressType, country
        );
        return repository.findBySellerIdAndAddressTypeAndCountry(
                        UUID.fromString(sellerId.toString()),
                        addressType.name(),
                        country)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> findBySellerIdWithPagination(SellerId sellerId, int page, int size) {
        log.debug("Finding addresses for seller: {}, page: {}, size: {}", sellerId, page, size);
        return repository.findBySellerId(
                        UUID.fromString(sellerId.toString()),
                        PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsById(String addressId) {
        return repository.existsById(UUID.fromString(addressId));
    }

    @Override
    public Mono<Boolean> existsBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        return repository.existsBySellerIdAndAddressType(
                UUID.fromString(sellerId.toString()),
                addressType.name());
    }

    @Override
    public Mono<Boolean> hasPickupAddress(SellerId sellerId) {
        return existsBySellerIdAndType(sellerId, AddressType.PICKUP);
    }

    @Override
    public Mono<Boolean> hasReturnAddress(SellerId sellerId) {
        return existsBySellerIdAndType(sellerId, AddressType.RETURN);
    }

    @Override
    public Mono<Boolean> hasWarehouseAddresses(SellerId sellerId) {
        return countWarehouseAddressesBySellerId(sellerId)
                .map(count -> count > 0);
    }

    @Override
    public Mono<Long> countBySellerId(SellerId sellerId) {
        return repository.countBySellerId(UUID.fromString(sellerId.toString()));
    }

    @Override
    public Mono<Long> countBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        return repository.countBySellerIdAndAddressType(
                UUID.fromString(sellerId.toString()),
                addressType.name());
    }

    @Override
    public Mono<Long> countWarehouseAddressesBySellerId(SellerId sellerId) {
        return countBySellerIdAndType(sellerId, AddressType.WAREHOUSE);
    }

    @Override
    public Mono<Long> countBySellerIdAndCountry(SellerId sellerId, String country) {
        return repository.countBySellerIdAndCountry(
                UUID.fromString(sellerId.toString()),
                country);
    }

    @Override
    public Flux<Address> saveAll(SellerId sellerId, List<Address> addresses) {
        log.debug("Saving {} addresses for seller: {}", addresses.size(), sellerId);
        LocalDateTime now = LocalDateTime.now();
        List<AddressEntity> entities = addresses.stream()
                .map(mapper::toEntity)
                .map(entity -> entity
                        .withSellerId(UUID.fromString(sellerId.toString()))
                        .withCreatedAt(now)
                        .withUpdatedAt(now))
                .toList();
        return repository.saveAll(entities)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> updateAll(List<Address> addresses) {
        log.debug("Updating {} addresses", addresses.size());
        LocalDateTime now = LocalDateTime.now();
        List<AddressEntity> entities = addresses.stream()
                .map(mapper::toEntity)
                .map(entity -> entity.withUpdatedAt(now))
                .toList();
        return repository.saveAll(entities)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteAllByIds(List<String> addressIds) {
        log.debug("Deleting {} addresses", addressIds.size());
        List<UUID> uuids = addressIds.stream()
                .map(UUID::fromString)
                .toList();
        return repository.deleteAllById(uuids);
    }

    @Override
    public Mono<Void> deleteAllBySellerIdAndType(SellerId sellerId, AddressType addressType) {
        return deleteBySellerIdAndType(sellerId, addressType);
    }

    @Override
    public Flux<Address> findBySellerIdAndLocationWithin(SellerId sellerId,
                                                         Double latitude,
                                                         Double longitude,
                                                         Double radiusInKm
    ) {
        log.debug("Finding addresses for seller: {} within {} km of ({}, {})",
                sellerId, radiusInKm, latitude, longitude);

        return repository.findBySellerId(UUID.fromString(sellerId.toString()))
                .filter(entity -> {
                    if (entity.latitude() == null || entity.longitude() == null) {
                        return false;
                    }
                    double distance = haversine(
                            latitude, longitude,
                            entity.latitude(), entity.longitude()
                    );
                    return distance <= radiusInKm;
                })
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Address> findBySellerIdAndCityAndLocationWithin(SellerId sellerId,
                                                                String city,
                                                                Double latitude,
                                                                Double longitude,
                                                                Double radiusInKm
    ) {
        log.debug("Finding addresses for seller: {}, city: {} within {} km", sellerId, city, radiusInKm);

        return repository.findBySellerIdAndCity(UUID.fromString(sellerId.toString()), city)
                .filter(entity -> {
                    if (entity.latitude() == null || entity.longitude() == null) {
                        return false;
                    }
                    double distance = haversine(
                            latitude, longitude,
                            entity.latitude(), entity.longitude()
                    );
                    return distance <= radiusInKm;
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> isValidAddress(Address address) {
        if (address == null) {
            return Mono.just(false);
        }

        boolean isValid = address.addressLine1() != null && !address.addressLine1().isBlank()
                && address.city() != null && !address.city().isBlank()
                && address.state() != null && !address.state().isBlank()
                && address.postalCode() != null && !address.postalCode().isBlank()
                && address.country() != null && !address.country().isBlank()
                && address.addressType() != null;

        return Mono.just(isValid);
    }

    @Override
    public Mono<Boolean> isDefaultAddress(SellerId sellerId, String addressId, AddressType addressType) {
        log.debug("Checking if address is default: {}, type: {}", addressId, addressType);
        return repository.findById(UUID.fromString(addressId))
                .filter(entity -> entity.sellerId().equals(UUID.fromString(sellerId.toString())))
                .filter(entity -> entity.addressType().equals(addressType.name()))
                .map(AddressEntity::isDefault)
                .defaultIfEmpty(false);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
