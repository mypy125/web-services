package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.domain.service.SellerDomainService;
import com.mygitgor.seller_service.infrastructure.kafka.producer.SellerEventProducer;
import com.mygitgor.seller_service.shared.exception.SellerNotFoundException;
import com.mygitgor.seller_service.shared.valueobject.AccountStatus;
import com.mygitgor.seller_service.shared.valueobject.ReportPeriod;
import com.mygitgor.seller_service.domain.model.statistic.ReportStatistics;
import com.mygitgor.seller_service.shared.valueobject.SellerVerificationStatus;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerAdminService {
    private final SellerRepositoryPort sellerRepository;
    private final SellerDomainService sellerDomainService;
    private final SellerEventProducer eventProducer;
    private final SellerReportService reportService;

    @Transactional
    public Mono<Seller> verifySellerBusiness(SellerId sellerId, String verifiedBy, String notes) {
        log.info("Admin '{}' is verifying business details for seller: {}", verifiedBy, sellerId);
        return findSeller(sellerId)
                .map(seller -> {
                    seller.verifyBusiness(verifiedBy, notes);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendBusinessVerifiedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller));
    }

    @Transactional
    public Mono<Seller> verifySellerTaxInfo(SellerId sellerId, String verifiedBy) {
        log.info("Admin '{}' is verifying tax information for seller: {}", verifiedBy, sellerId);
        return findSeller(sellerId)
                .map(seller -> {
                    seller.verifyTax(verifiedBy);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendTaxVerifiedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller));
    }

    @Transactional
    public Mono<Seller> rejectSellerVerification(SellerId sellerId, String reason, String rejectedBy) {
        log.warn("Admin '{}' rejected verification for seller: {}. Reason: {}", rejectedBy, sellerId, reason);
        return findSeller(sellerId)
                .map(seller -> {
                    seller.rejectVerification(reason, rejectedBy);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendVerificationRejectedEvent(seller, reason)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller));
    }

    @Transactional
    public Mono<Seller> activateSeller(SellerId sellerId, String activatedBy) {
        log.info("Admin '{}' is activating seller account: {}", activatedBy, sellerId);
        return findSeller(sellerId)
                .flatMap(seller -> sellerDomainService.activateSeller(seller, activatedBy))
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendSellerActivatedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller));
    }

    @Transactional
    public Mono<Seller> suspendSeller(SellerId sellerId, String reason, String suspendedBy) {
        log.warn("Admin '{}' is suspending seller: {}. Reason: {}", suspendedBy, sellerId, reason);
        return findSeller(sellerId)
                .flatMap(seller -> sellerDomainService.suspendSeller(seller, reason, suspendedBy))
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendSellerSuspendedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller));
    }

    @Transactional
    public Mono<Seller> banSeller(SellerId sellerId, String reason, String bannedBy) {
        log.error("CRITICAL: Admin '{}' is BANNING seller: {}. Reason: {}", bannedBy, sellerId, reason);
        return findSeller(sellerId)
                .flatMap(seller -> sellerDomainService.banSeller(seller, reason, bannedBy))
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendSellerBannedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller));
    }

    @Transactional
    public Mono<Seller> updateSellerCommissionRate(SellerId sellerId, Double newRate, String updatedBy) {
        log.info("Admin '{}' updating commission rate to {}% for seller: {}", updatedBy, newRate, sellerId);
        return findSeller(sellerId)
                .map(seller -> {
                    seller.updateCommission(newRate, updatedBy);
                    return seller;
                })
                .flatMap(sellerRepository::save)
                .flatMap(seller -> eventProducer.sendSellerUpdatedEvent(seller)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .thenReturn(seller));
    }

    @Transactional
    public Mono<Seller> updateSellerCashbackRate(SellerId sellerId, Double cashbackRate) {
        log.info("Admin updating cashback rate to {}% for seller: {}", cashbackRate, sellerId);
        return findSeller(sellerId)
                .map(seller -> {
                    seller.updateCashback(cashbackRate);
                    return seller;
                })
                .flatMap(sellerRepository::save);
    }

    public Mono<Seller> getSellerDetailsForAdmin(SellerId sellerId) {
        return findSeller(sellerId);
    }

    public Flux<Seller> getSellersByStatus(AccountStatus accountStatus, int page, int size) {
        log.debug("Admin fetching sellers with account status: {}", accountStatus);
        return sellerRepository.findAllByAccountStatus(accountStatus, page, size);
    }

    public Flux<Seller> getSellersByVerificationStatus(SellerVerificationStatus status, int page, int size) {
        log.debug("Admin fetching sellers with verification status: {}", status);
        return sellerRepository.findAllByVerificationStatus(status, page, size);
    }

    public Flux<SellerReport> generateReportsForAllSellers(ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Admin generating reports for all sellers");
        return reportService.generateReportsForAllSellers(period, startDate, endDate);
    }

    public Mono<ReportStatistics> getGlobalReportStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting global report statistics");
        return reportService.getReportStatistics(null, startDate, endDate);
    }

    private Mono<Seller> findSeller(SellerId sellerId) {
        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new SellerNotFoundException(sellerId.toString())));
    }
}
