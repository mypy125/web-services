package com.mygitgor.seller_service.infrastructure.kafka.producer;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.domain.model.Transaction;
import com.mygitgor.seller_service.domain.model.shared.valueobject.Email;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.port.outgoing.KafkaEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerEventProducer implements KafkaEventPort {
    @Override
    public Mono<Void> sendSellerRegisteredEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerDeletedEvent(SellerId sellerId, Email email) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerActivatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerSuspendedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerBannedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendEmailVerifiedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendBusinessVerifiedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendVerificationRejectedEvent(Seller seller, String reason) {
        return null;
    }

    @Override
    public Mono<Void> sendBusinessDetailsUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendBankDetailsUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendCommissionRateUpdatedEvent(Seller seller, Double oldRate, Double newRate) {
        return null;
    }

    @Override
    public Mono<Void> sendOrderStatsUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendRatingUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerReportGeneratedEvent(SellerReport report) {
        return null;
    }

    @Override
    public Mono<Void> sendTransactionCreatedEvent(Transaction transaction) {
        return null;
    }

    @Override
    public Mono<Void> sendTransactionUpdatedEvent(Transaction transaction) {
        return null;
    }

    @Override
    public Mono<Void> sendBulkSellerUpdateEvent(List<Seller> sellers, String action) {
        return null;
    }

    @Override
    public Mono<Void> sendBulkVerificationEvent(List<SellerId> sellerIds, String verifiedBy) {
        return null;
    }
}
