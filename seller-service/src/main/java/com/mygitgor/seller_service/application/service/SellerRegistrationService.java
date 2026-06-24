package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.request.RegisterSellerRequest;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.valueobject.Email;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.domain.service.SellerDomainService;
import com.mygitgor.seller_service.infrastructure.kafka.producer.SellerEventProducer;
import com.mygitgor.seller_service.infrastructure.mapper.SellerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class SellerRegistrationService {
    private final SellerRepositoryPort sellerRepository;
    private final SellerDomainService sellerDomainService;
    private final SellerEventProducer eventProducer;
    private final SellerMapper mapper;

    public Mono<SellerRegistrationResponse> registerSeller(RegisterSellerRequest req) {
        log.info("Registering new seller with email: {}", req.email());

        Email email = new Email(req.email());

        return sellerDomainService.validateEmailUniqueness(email)
                .then(Mono.fromCallable(() -> {
                    Seller seller = Seller.register(
                            email,
                            req.sellerName(),
                            req.mobile(),
                            req.businessDetails(),
                            req.bankDetails(),
                            req.pickupAddress()
                    );
                    return seller;
                }))
                .flatMap(sellerRepository::save)
                .flatMap(seller ->
                        sellerDomainService.isReadyToSell(seller)
                                .map(isReady -> {
                                    if (isReady) {
                                        log.info("Seller {} is ready to sell", seller.getEmail());
                                    }
                                    return seller;
                                })
                )
                .doOnSuccess(seller -> {
                    eventProducer.sendSellerRegisteredEvent(seller).subscribe(
                            success -> log.debug("Seller registered event sent to Kafka"),
                            error -> log.error("Failed to send seller registered event", error)
                    );
                })
                .map(mapper::toRegistrationResponse)
                .doOnSuccess(response -> log.info("Seller registered successfully: {}", req.email()))
                .doOnError(error -> log.error("Failed to register seller: {}", req.email(), error));
    }
}
