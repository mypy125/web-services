package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.request.RegisterSellerRequest;
import com.mygitgor.seller_service.application.dto.request.UploadDocumentRequest;
import com.mygitgor.seller_service.application.dto.response.SellerRegistrationResponse;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.infrastructure.cache.SellerCacheService;
import com.mygitgor.seller_service.shared.exception.DomainException;
import com.mygitgor.seller_service.shared.exception.SellerNotFoundException;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.domain.service.SellerDomainService;
import com.mygitgor.seller_service.infrastructure.kafka.producer.SellerEventProducer;
import com.mygitgor.seller_service.infrastructure.mapper.SellerMapper;
import com.mygitgor.seller_service.domain.model.status.SellerVerificationStatus;
import com.mygitgor.seller_service.shared.valueobject.VerificationDocument;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerRegistrationService {
    private final SellerRepositoryPort sellerRepository;
    private final SellerCacheService cacheService;
    private final SellerDomainService sellerDomainService;
    private final SellerEventProducer eventProducer;
    private final SellerMapper mapper;

    @Transactional
    public Mono<SellerRegistrationResponse> registerSeller(RegisterSellerRequest request) {
        log.info("Registering new seller with email: {}", request.email());

        Email email = new Email(request.email());

        return sellerDomainService.validateEmailUniqueness(email)
                .then(Mono.fromCallable(() -> Seller.register(
                            email,
                            request.sellerName(),
                            request.mobile(),
                            request.businessDetails(),
                            request.bankDetails(),
                            request.pickupAddress()
                    )))
                .flatMap(sellerRepository::save)
                .flatMap(seller -> cacheService.cacheSellerById(seller)
                        .then(cacheService.evictUserDashboardCache(seller.getSellerId()))
                        .thenReturn(seller))
                .flatMap(seller ->eventProducer.sendSellerRegisteredEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> {
                            log.error("Failed to send seller registered event for {}", seller.getSellerId(), err);
                            return Mono.empty();
                        })
                        .thenReturn(seller)
                )
                .map(mapper::toRegistrationResponse)
                .doOnSuccess(res -> log.info("Seller registered successfully, ID: {}", res.id()))
                .doOnError(err -> log.error("Failed to register seller: {}", request.email(), err));

    }

    @Transactional
    public Mono<SellerRegistrationResponse> verifyEmailToken(String emailStr, String token) {
        log.info("Verifying email token for: {}", emailStr);
        Email email = new Email(emailStr);

        return sellerRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(emailStr)))
                .flatMap(seller -> sellerDomainService.verifyRegistrationToken(seller, token)
                        .then(Mono.fromRunnable(seller::verifyEmail))
                        .then(sellerRepository.save(seller))
                )
                .flatMap(seller -> cacheService.cacheSellerById(seller)
                        .then(cacheService.evictUserDashboardCache(seller.getSellerId()))
                        .thenReturn(seller))
                .flatMap(seller -> eventProducer.sendEmailVerifiedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> {
                            log.error("Failed to send EmailVerifiedEvent for seller: {}", seller.getSellerId(), err);
                            return Mono.empty();
                        })
                        .thenReturn(seller)
                )
                .map(mapper::toRegistrationResponse);
    }

    public Mono<Void> resendVerificationEmail(String emailStr) {
        log.info("Request to resend verification email for: {}", emailStr);

        return sellerRepository.findByEmail(new Email(emailStr))
                .switchIfEmpty(Mono.error(new SellerNotFoundException(emailStr)))
                .flatMap(seller -> {
                    if (seller.isEmailVerified()) {
                        return Mono.error(new DomainException("Email is already verified"));
                    }
                    return eventProducer.sendEmailVerificationRequestedEvent(seller)
                            .subscribeOn(Schedulers.boundedElastic())
                            .thenReturn(seller);
                })
                .then();
    }

    @Transactional
    public Mono<SellerRegistrationResponse> uploadVerificationDocuments(SellerId sellerId, UploadDocumentRequest request) {
        log.info("Uploading verification documents for seller: {}", sellerId);

        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())))
                .map(seller -> {
                    VerificationDocument document = VerificationDocument.builder()
                            .documentType(request.documentType())
                            .documentUrl(request.documentUrl())
                            .documentNumber(request.documentNumber())
                            .documentName(request.documentType() + "_doc")
                            .issuedDate(LocalDateTime.now())
                            .verificationStatus("PENDING")
                            .metadata(new HashMap<>())
                            .build();

                    seller.setVerificationDocument(document);
                    seller.setVerificationStatus(SellerVerificationStatus.PENDING);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> cacheService.cacheSellerById(seller)
                        .then(cacheService.evictUserDashboardCache(seller.getSellerId()))
                        .thenReturn(seller))
                .flatMap(seller -> eventProducer.sendDocumentsUploadedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> {
                            log.error("Failed to send DocumentsUploadedEvent for seller: {}", sellerId, err);
                            return Mono.empty();
                        })
                        .thenReturn(seller)
                )
                .map(mapper::toRegistrationResponse);
    }
}
