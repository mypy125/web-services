package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.shared.exception.DomainException;
import com.mygitgor.seller_service.shared.exception.SellerNotFoundException;
import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.model.type.AddressType;
import com.mygitgor.seller_service.domain.model.statistic.AddressStatistics;
import com.mygitgor.seller_service.domain.repository.AddressRepositoryPort;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.domain.specification.AddressCompositeSpec;
import com.mygitgor.seller_service.domain.specification.AddressUniquenessSpec;
import com.mygitgor.seller_service.domain.specification.AddressValidationSpec;
import com.mygitgor.seller_service.infrastructure.kafka.producer.SellerEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerAddressService {
    private final AddressRepositoryPort addressRepository;
    private final SellerRepositoryPort sellerRepository;
    private final SellerEventProducer eventProducer;
    private final AddressCompositeSpec addressCompositeSpec;
    private final AddressValidationSpec validationSpec;
    private final AddressUniquenessSpec uniquenessSpec;

    @Transactional
    public Mono<Address> addPickupAddress(SellerId sellerId, Address address) {
        log.info("Adding pickup address for seller: {}", sellerId);

        return addressCompositeSpec.validateAddressByType(address, AddressType.PICKUP)
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.error(new DomainException("Invalid pickup address")))
                .flatMap(isValid -> addressCompositeSpec.canAddAddressOfType(sellerId, AddressType.PICKUP))
                .filter(canAdd -> canAdd)
                .switchIfEmpty(Mono.error(new DomainException("Pickup address already exists for this seller")))
                .flatMap(canAdd -> addressCompositeSpec.validateAddressForCreation(sellerId, address))
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.error(new DomainException("Address is not unique or invalid")))
                .flatMap(isValid -> sellerRepository.findById(sellerId))
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> {
                    seller.updatePickupAddress(address);
                    return sellerRepository.save(seller);
                })
                .flatMap(seller -> addressRepository.saveForSeller(sellerId, address))
                .flatMap(addr -> eventProducer.sendAddressAddedEvent(sellerId, addr, AddressType.PICKUP)
                        .thenReturn(addr));
    }

    @Transactional
    public Mono<Address> addReturnAddress(SellerId sellerId, Address address) {
        log.info("Adding return address for seller: {}", sellerId);

        return addressCompositeSpec.validateAddressByType(address, AddressType.RETURN)
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.error(new DomainException("Invalid return address")))
                .flatMap(isValid -> addressCompositeSpec.canAddAddressOfType(sellerId, AddressType.RETURN))
                .filter(canAdd -> canAdd)
                .switchIfEmpty(Mono.error(new DomainException("Return address already exists for this seller")))
                .flatMap(canAdd -> addressCompositeSpec.validateAddressForCreation(sellerId, address))
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.error(new DomainException("Address is not unique or invalid")))
                .flatMap(isValid -> sellerRepository.findById(sellerId))
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> {
                    seller.updateReturnAddress(address);
                    return sellerRepository.save(seller);
                })
                .flatMap(seller -> addressRepository.saveForSeller(sellerId, address))
                .flatMap(addr -> eventProducer.sendAddressAddedEvent(sellerId, addr, AddressType.RETURN)
                        .thenReturn(addr));
    }

    @Transactional
    public Mono<Address> addWarehouseAddress(SellerId sellerId, Address address) {
        log.info("Adding warehouse address for seller: {}", sellerId);

        return addressCompositeSpec.validateAddressByType(address, AddressType.WAREHOUSE)
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.error(new DomainException("Invalid warehouse address")))
                .flatMap(isValid -> addressCompositeSpec.canAddMoreWarehouseAddresses(sellerId, 10))
                .filter(canAdd -> canAdd)
                .switchIfEmpty(Mono.error(new DomainException("Maximum warehouse addresses limit reached (10)")))
                .flatMap(canAdd -> addressCompositeSpec.validateAddressForCreation(sellerId, address))
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.error(new DomainException("Address is not unique or invalid")))
                .flatMap(isValid -> sellerRepository.findById(sellerId))
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> {
                    seller.addWarehouseAddress(address);
                    return sellerRepository.save(seller);
                })
                .flatMap(seller -> addressRepository.saveForSeller(sellerId, address))
                .flatMap(addr -> eventProducer.sendAddressAddedEvent(sellerId, addr, AddressType.WAREHOUSE)
                        .thenReturn(addr));
    }

    public Mono<Address> getPickupAddress(SellerId sellerId) {
        log.debug("Getting pickup address for seller: {}", sellerId);
        return addressRepository.findPickupAddressBySellerId(sellerId);
    }

    public Mono<Address> getReturnAddress(SellerId sellerId) {
        log.debug("Getting return address for seller: {}", sellerId);
        return addressRepository.findReturnAddressBySellerId(sellerId);
    }

    public Flux<Address> getWarehouseAddresses(SellerId sellerId) {
        log.debug("Getting warehouse addresses for seller: {}", sellerId);
        return addressRepository.findWarehouseAddressesBySellerId(sellerId);
    }

    public Flux<Address> getAddressesByType(SellerId sellerId, AddressType addressType) {
        log.debug("Getting addresses by type {} for seller: {}", addressType, sellerId);
        return addressRepository.findBySellerIdAndType(sellerId, addressType);
    }

    public Flux<Address> getAllAddresses(SellerId sellerId) {
        log.debug("Getting all addresses for seller: {}", sellerId);
        return addressRepository.findBySellerId(sellerId);
    }

    @Transactional
    public Mono<Address> updateAddress(SellerId sellerId, Address address) {
        log.info("Updating address for seller: {}", sellerId);

        return addressCompositeSpec.validateAddressCompletely(address)
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.error(new DomainException("Invalid address")))
                .flatMap(isValid -> sellerRepository.findById(sellerId))
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> {
                    if (address.addressType() == AddressType.PICKUP) {
                        seller.updatePickupAddress(address);
                    } else if (address.addressType() == AddressType.RETURN) {
                        seller.updateReturnAddress(address);
                    } else if (address.addressType() == AddressType.WAREHOUSE) {
                        if (seller.getWarehouseAddresses() != null) {
                            seller.getWarehouseAddresses().removeIf(a -> a.id().equals(address.id()));
                            seller.getWarehouseAddresses().add(address);
                        }
                    }
                    return sellerRepository.save(seller);
                })
                .flatMap(seller -> addressRepository.save(address))
                .flatMap(addr -> eventProducer.sendAddressUpdatedEvent(sellerId, addr)
                        .thenReturn(addr));
    }

    @Transactional
    public Mono<Void> deleteAddress(SellerId sellerId, String addressId) {
        log.info("Deleting address {} for seller: {}", addressId, sellerId);

        return addressRepository.findById(addressId)
                .switchIfEmpty(Mono.error(new DomainException("Address not found: " + addressId)))
                .flatMap(address -> {
                    if (address.addressType() == AddressType.PICKUP) {
                        return Mono.error(new DomainException("Cannot delete default pickup address"));
                    }
                    if (address.addressType() == AddressType.RETURN) {
                        return Mono.error(new DomainException("Cannot delete default return address"));
                    }
                    return sellerRepository.findById(sellerId);
                })
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> {
                    if (seller.getWarehouseAddresses() != null) {
                        seller.getWarehouseAddresses().removeIf(a -> a.id().equals(addressId));
                        return sellerRepository.save(seller);
                    }
                    return Mono.just(seller);
                })
                .flatMap(seller -> addressRepository.deleteById(addressId));
    }

    @Transactional
    public Mono<Void> deleteAddressesByType(SellerId sellerId, AddressType addressType) {
        log.info("Deleting all {} addresses for seller: {}", addressType, sellerId);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .flatMap(seller -> {
                    if (addressType == AddressType.WAREHOUSE) {
                        seller.getWarehouseAddresses().clear();
                        return sellerRepository.save(seller);
                    } else if (addressType == AddressType.PICKUP) {
                        return Mono.error(new DomainException("Cannot delete pickup address"));
                    } else if (addressType == AddressType.RETURN) {
                        return Mono.error(new DomainException("Cannot delete return address"));
                    }
                    return Mono.just(seller);
                })
                .flatMap(seller -> addressRepository.deleteBySellerIdAndType(sellerId, addressType));
    }

    public Mono<AddressStatistics> getAddressStatistics(SellerId sellerId) {
        log.debug("Getting address statistics for seller: {}", sellerId);

        return Mono.zip(
                addressRepository.countBySellerId(sellerId),
                addressRepository.countBySellerIdAndType(sellerId, AddressType.PICKUP),
                addressRepository.countBySellerIdAndType(sellerId, AddressType.RETURN),
                addressRepository.countBySellerIdAndType(sellerId, AddressType.WAREHOUSE),
                addressRepository.countBySellerIdAndType(sellerId, AddressType.OFFICE),
                addressRepository.findPickupAddressBySellerId(sellerId).map(Optional::of).defaultIfEmpty(Optional.empty()),
                addressRepository.findReturnAddressBySellerId(sellerId).map(Optional::of).defaultIfEmpty(Optional.empty())
        ).map(tuple -> AddressStatistics.builder()
                .totalAddresses(tuple.getT1())
                .pickupAddressCount(tuple.getT2())
                .returnAddressCount(tuple.getT3())
                .warehouseAddressCount(tuple.getT4())
                .officeAddressCount(tuple.getT5())
                .defaultPickupAddress(tuple.getT6().orElse(null))
                .defaultReturnAddress(tuple.getT7().orElse(null))
                .calculatedAt(LocalDateTime.now())
                .build()
        );
    }

    public Mono<Boolean> validateAddress(Address address) {
        return addressCompositeSpec.validateAddressCompletely(address);
    }

    public Mono<Boolean> validateAddressByType(Address address, AddressType expectedType) {
        return addressCompositeSpec.validateAddressByType(address, expectedType);
    }

    public Mono<Boolean> isAddressUnique(SellerId sellerId, Address address) {
        return uniquenessSpec.isAddressUnique(sellerId, address);
    }

    public Mono<Boolean> hasAllRequiredAddresses(SellerId sellerId) {
        return addressCompositeSpec.hasAllRequiredAddresses(sellerId);
    }

    public Mono<Boolean> isCorrectAddressType(Address address, AddressType expectedType) {
        return validationSpec.isCorrectAddressType(address, expectedType);
    }
}