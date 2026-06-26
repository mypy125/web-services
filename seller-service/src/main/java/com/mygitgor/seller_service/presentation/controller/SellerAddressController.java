package com.mygitgor.seller_service.presentation.controller;

import com.mygitgor.seller_service.application.dto.request.AddressRequest;
import com.mygitgor.seller_service.application.dto.response.AddressResponse;
import com.mygitgor.seller_service.application.service.SellerAddressService;
import com.mygitgor.seller_service.domain.model.shared.valueobject.Address;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.model.statistic.AddressStatistics;
import com.mygitgor.seller_service.infrastructure.mapper.SellerMapper;
import com.mygitgor.seller_service.infrastructure.sequrity.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Slf4j
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/addresses")
@RequiredArgsConstructor
public class SellerAddressController {
    private final SellerAddressService addressService;
    private final SellerMapper mapper;

    @PostMapping("/pickup")
    public Mono<AddressResponse> addPickupAddress(@PathVariable String sellerId,
                                                  @Valid @RequestBody AddressRequest request,
                                                  @AuthenticationPrincipal AuthUser currentUser
    ) {

        validateOwnership(sellerId, currentUser);
        Address address = mapper.toAddress(request);
        return addressService.addPickupAddress(new SellerId(sellerId), address)
                .map(mapper::toAddressResponse);
    }

    @PostMapping("/return")
    public Mono<AddressResponse> addReturnAddress(@PathVariable String sellerId,
                                                  @Valid @RequestBody AddressRequest request,
                                                  @AuthenticationPrincipal AuthUser currentUser
    ) {
        validateOwnership(sellerId, currentUser);
        Address address = mapper.toAddress(request);
        return addressService.addReturnAddress(new SellerId(sellerId), address)
                .map(mapper::toAddressResponse);
    }

    @PostMapping("/warehouse")
    public Mono<AddressResponse> addWarehouseAddress(@PathVariable String sellerId,
                                                     @Valid @RequestBody AddressRequest request,
                                                     @AuthenticationPrincipal AuthUser currentUser
    ) {
        validateOwnership(sellerId, currentUser);
        Address address = mapper.toAddress(request);
        return addressService.addWarehouseAddress(new SellerId(sellerId), address)
                .map(mapper::toAddressResponse);
    }

    @GetMapping("/pickup")
    public Mono<AddressResponse> getPickupAddress(@PathVariable String sellerId,
                                                  @AuthenticationPrincipal AuthUser currentUser
    ) {
        validateOwnership(sellerId, currentUser);
        return addressService.getPickupAddress(new SellerId(sellerId))
                .map(mapper::toAddressResponse);
    }

    @GetMapping("/return")
    public Mono<AddressResponse> getReturnAddress(@PathVariable String sellerId,
                                                  @AuthenticationPrincipal AuthUser currentUser
    ) {
        validateOwnership(sellerId, currentUser);
        return addressService.getReturnAddress(new SellerId(sellerId))
                .map(mapper::toAddressResponse);
    }

    @GetMapping("/warehouse")
    public Flux<AddressResponse> getWarehouseAddresses(@PathVariable String sellerId,
                                                       @AuthenticationPrincipal AuthUser currentUser
    ) {
        validateOwnership(sellerId, currentUser);
        return addressService.getWarehouseAddresses(new SellerId(sellerId))
                .map(mapper::toAddressResponse);
    }

    @GetMapping
    public Flux<AddressResponse> getAllAddresses(@PathVariable String sellerId,
                                                 @AuthenticationPrincipal AuthUser currentUser
    ) {
        validateOwnership(sellerId, currentUser);
        return addressService.getAllAddresses(new SellerId(sellerId))
                .map(mapper::toAddressResponse);
    }

    @GetMapping("/statistics")
    public Mono<AddressStatistics> getAddressStatistics(@PathVariable String sellerId,
                                                        @AuthenticationPrincipal AuthUser currentUser
    ) {
        validateOwnership(sellerId, currentUser);
        return addressService.getAddressStatistics(new SellerId(sellerId));
    }

    @DeleteMapping("/{addressId}")
    public Mono<Void> deleteAddress(@PathVariable String sellerId,
                                    @PathVariable String addressId,
                                    @AuthenticationPrincipal AuthUser currentUser
    ) {
        validateOwnership(sellerId, currentUser);
        return addressService.deleteAddress(new SellerId(sellerId), addressId);
    }

    private void validateOwnership(String sellerId, AuthUser currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("User session not found");
        }
        if (!currentUser.getUserId().equalsIgnoreCase(sellerId) && !currentUser.isAdmin()) {
            log.warn("Access denied for user {} attempting to manage addresses of seller {}", currentUser.getUserId(), sellerId);
            throw new AccessDeniedException("You do not have permission to manage addresses for this seller account");
        }
    }
}